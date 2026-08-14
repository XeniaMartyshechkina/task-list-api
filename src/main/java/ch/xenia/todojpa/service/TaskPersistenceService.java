package ch.xenia.todojpa.service;

import ch.xenia.todojpa.domain.Account;
import ch.xenia.todojpa.domain.Task;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class TaskPersistenceService {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Task persistTask(Task task, Long accountId, String email) {
        Account account = entityManager.createQuery("""
            SELECT a FROM Person p 
            JOIN p.accounts a 
            WHERE p.email = :email
            AND a.id = :accountId 
            """, Account.class)
                .setParameter("email", email)
                .setParameter("accountId", accountId)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        account.addTask(task);
        return task;
    }
}
