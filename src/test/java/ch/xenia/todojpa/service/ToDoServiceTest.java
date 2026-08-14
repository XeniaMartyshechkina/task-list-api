package ch.xenia.todojpa.service;

import ch.xenia.todojpa.TestDataFactory;
import ch.xenia.todojpa.domain.*;
import ch.xenia.todojpa.web.dto.request.CreateTaskRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ToDoServiceTest {

    @Autowired
    private ToDoPersistenceService toDoService;

    @PersistenceContext
    private EntityManager entityManager;

    @MockitoBean
    private AiTaskAnalysisService aiTaskAnalysisService;

    @Test
    void shouldCreatePerson() {
        Person person = toDoService.createPerson(TestDataFactory.createPersonRequest(TestDataFactory.person1));
        assertNotNull(person);
        assertEquals(TestDataFactory.person1[0], person.getEmail());
        assertEquals(TestDataFactory.person1[2], person.getLastName());
    }
    @Test
    void shouldCreateAccountForPerson() {
        Person person = toDoService.createPerson(TestDataFactory.createPersonRequest(TestDataFactory.person1));
        Account account = toDoService.createAccountForAPerson(TestDataFactory.createAccountRequest(TestDataFactory.account1), person.getEmail());
        assertNotNull(account);
        entityManager.flush();
        entityManager.clear();
        Person personWithAccount = toDoService.findPersonByEmail(person.getEmail());
        assertEquals(account.getId(), personWithAccount.getAccounts().getFirst().getId());
    }

    @Test
    void shouldCreateTaskForExistingAccount() {
        TaskElementsFromAi aiResponse = TestDataFactory.createAiTaskResponse();

        when(aiTaskAnalysisService.analyzeTask(any(CreateTaskRequest.class)))
                .thenReturn(aiResponse);

        Person person = toDoService.createPerson(TestDataFactory.createPersonRequest(TestDataFactory.person1));
        Account account = toDoService.createAccountForAPerson(TestDataFactory.createAccountRequest(TestDataFactory.account1), person.getEmail());
        entityManager.flush();
        entityManager.clear();
        Task task = toDoService.createTask(TestDataFactory.createTaskRequest(TestDataFactory.task), account.getId(), person.getEmail());
        assertNotNull(task);
        assertEquals(TestDataFactory.task, task.getDescription());
    }

    @Test
    void shouldChangeAccountStatus() {
        Person person = toDoService.createPerson(TestDataFactory.createPersonRequest(TestDataFactory.person1));
        Account account = toDoService.createAccountForAPerson(TestDataFactory.createAccountRequest(TestDataFactory.account1), person.getEmail());
        entityManager.flush();
        entityManager.clear();
        toDoService.updateAccountStatus(account.getId(), AccountStatusEnum.BLOCKED);
        Account result = toDoService.findAccountById(account.getId());
        assertNotNull(result);
        assertEquals(AccountStatusEnum.BLOCKED, result.getStatus());
    }

    @Test
    void shouldFindPerson() {
        TaskElementsFromAi aiResponse = TestDataFactory.createAiTaskResponse();

        when(aiTaskAnalysisService.analyzeTask(any(CreateTaskRequest.class)))
                .thenReturn(aiResponse);

        Person person = toDoService.createPerson(TestDataFactory.createPersonRequest(TestDataFactory.person1));
        Account account = toDoService.createAccountForAPerson(TestDataFactory.createAccountRequest(TestDataFactory.account1), person.getEmail());
        entityManager.flush();
        entityManager.clear();
        toDoService.createTask(TestDataFactory.createTaskRequest(TestDataFactory.task), account.getId(), person.getEmail());
        Person result = toDoService.findPersonByEmail(TestDataFactory.person1[0]);
        System.out.println(result);
        assertNotNull(result);
        assertEquals(TestDataFactory.person1[1], result.getFirstName());
        assertEquals(1, result.getAccounts().size());
        assertEquals(1, result.getAccounts().getFirst().getTasks().size());
    }

    @Test
    void shouldFindAccountsWithStatus() {
        Person person = toDoService.createPerson(TestDataFactory.createPersonRequest(TestDataFactory.person1));
        Account accountActive = toDoService.createAccountForAPerson(TestDataFactory.createAccountRequest(TestDataFactory.account1), person.getEmail());
        Account accountBlocked = toDoService.createAccountForAPerson(TestDataFactory.createAccountRequest(TestDataFactory.account2), person.getEmail());
        accountBlocked.setStatus(AccountStatusEnum.BLOCKED);
        entityManager.flush();
        entityManager.clear();
        List<Account> activeAccounts = toDoService.findAccountsWithStatus(AccountStatusEnum.ACTIVE);
        List<Account> blockedAccounts = toDoService.findAccountsWithStatus(AccountStatusEnum.BLOCKED);
        assertEquals(1, activeAccounts.size());
        assertEquals(1, blockedAccounts.size());
    }

    @Test
    void shouldFindAllPersons() {
        toDoService.createPerson(TestDataFactory.createPersonRequest(TestDataFactory.person1));
        toDoService.createPerson(TestDataFactory.createPersonRequest(TestDataFactory.person2));
        entityManager.flush();
        entityManager.clear();
        List<Person> res = toDoService.findAllPersons();
        assertNotNull(res);
        assertEquals(3, res.size());
    }
}
