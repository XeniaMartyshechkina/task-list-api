package ch.xenia.todojpa.web.controller;

import ch.xenia.todojpa.domain.Account;
import ch.xenia.todojpa.domain.AccountStatusEnum;
import ch.xenia.todojpa.domain.Person;
import ch.xenia.todojpa.service.ToDoPersistenceService;
import ch.xenia.todojpa.web.dto.response.PersonResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RetrievalController {

    private final ToDoPersistenceService toDoService;

    public RetrievalController(ToDoPersistenceService toDoService) {
        this.toDoService = toDoService;
    }

    @GetMapping("/me")
    public PersonResponse findPersonByEmail(Authentication authentication) {
        return PersonResponse.from(toDoService.findPersonByEmail(authentication.getName()));
    }

    @GetMapping("/persons")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Person> findAllPersons() {
        return toDoService.findAllPersons();
    }

    @GetMapping("/accounts")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Account> findAllAccountsByStatus(@RequestParam AccountStatusEnum status) {
        return toDoService.findAccountsWithStatus(status);
    }
}
