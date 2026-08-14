package ch.xenia.todojpa.service;

import ch.xenia.todojpa.domain.*;
import ch.xenia.todojpa.web.dto.request.CreateAccountRequest;
import ch.xenia.todojpa.web.dto.request.CreatePersonRequest;
import ch.xenia.todojpa.web.dto.request.CreateTaskRequest;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToDoPersistenceService {
    @PersistenceContext
    private EntityManager entityManager;

    private final PasswordEncoder passwordEncoder;

    private final AiTaskAnalysisService aiToDoService;

    private final TaskPersistenceService taskPersistenceService;

    public ToDoPersistenceService(PasswordEncoder passwordEncoder, AiTaskAnalysisService aiToDoService, TaskPersistenceService taskPersistenceService) {
        this.passwordEncoder = passwordEncoder;
        this.aiToDoService = aiToDoService;
        this.taskPersistenceService = taskPersistenceService;
    }

    @Transactional
    public Person createPerson(CreatePersonRequest request) {
        Person existingPerson = entityManager.find(Person.class, request.email());
        if (existingPerson != null) {
            throw new EntityExistsException("Person with email: " + request.email() + " already exists");
        }
        Person person = new Person();
        person.setEmail(request.email());
        person.setFirstName(request.firstName());
        person.setLastName(request.lastName());
        person.setAddress(request.address());
        person.setRole(PersonRoleEnum.USER);
        person.setPasswordHash(passwordEncoder.encode(request.password()));
        entityManager.persist(person);
        return person;
    }

    @Transactional
    public Account createAccountForAPerson(CreateAccountRequest request, String email) {
        Person existingPerson = entityManager.find(Person.class, email);
        if (existingPerson == null) {
            throw new EntityNotFoundException("Person with email " + email + " not found");
        }
        Account account = new Account();
        account.setTitle(request.title());
        account.setStatus(AccountStatusEnum.ACTIVE);
        existingPerson.addAccount(account);
        return account;
    }

    public Task createTask(CreateTaskRequest request, Long accountId, String email) {
        TaskElementsFromAi aiResponse = aiToDoService.analyzeTask(request);
        Task task = new Task();
        task.setSummary(aiResponse.getSummary());
        task.setDescription(request.description());
        task.setStatus(TaskStatusEnum.TO_DO);
        task.setCategory(TaskCategoryEnum.valueOf(aiResponse.getCategory()));
        task.setPriority(TaskPriorityEnum.valueOf(aiResponse.getAiSuggestedPriority()));
        return taskPersistenceService.persistTask(task, accountId, email);
    }

    @Transactional
    public void updateTaskPriority(Long taskId, TaskPriorityEnum priority) {
        Task task = entityManager.find(Task.class, taskId);
        if (task == null) {
            throw new EntityNotFoundException(
                    "Task with id " + taskId + " not found"
            );
        }
        task.setPriority(priority);
    }

    @Transactional
    public void updateAccountStatus(Long accountId, AccountStatusEnum status) {
        Account account = entityManager.find(Account.class, accountId);
        if (account == null) {
            throw new EntityNotFoundException(
                    "Account with id " + accountId + " not found"
            );
        }
        account.setStatus(status);
    }

    @Transactional
    public Person findPersonByEmail(String email) {
        return entityManager.find(Person.class, email);
    }

    public List<Person> findAllPersons() {
        return entityManager.createQuery("SELECT p FROM Person p", Person.class).getResultList();
    }

    public List<Account> findAccountsWithStatus(AccountStatusEnum status) {
        return entityManager.createQuery("SELECT a FROM Account a WHERE a.status = :status", Account.class).setParameter("status", status).getResultList();
    }

    public Account findAccountById(Long accountId) {
        Account account = entityManager.find(Account.class, accountId);
        if (account == null) {
            throw new EntityNotFoundException("Account with id " + accountId + " not found");
        }
        return account;
    }

}
