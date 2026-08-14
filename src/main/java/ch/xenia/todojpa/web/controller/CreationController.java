package ch.xenia.todojpa.web.controller;

import ch.xenia.todojpa.service.ToDoPersistenceService;
import ch.xenia.todojpa.web.dto.request.*;
import ch.xenia.todojpa.web.dto.response.AccountResponse;
import ch.xenia.todojpa.web.dto.response.PersonResponse;
import ch.xenia.todojpa.web.dto.response.TaskResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CreationController {

    private final ToDoPersistenceService toDoService;

    public CreationController(ToDoPersistenceService toDoService) {
        this.toDoService = toDoService;
    }

    @PostMapping("/persons")
    @ResponseStatus(HttpStatus.CREATED)
    public PersonResponse createPerson(@Valid @RequestBody CreatePersonRequest request) {
        return PersonResponse.from(toDoService.createPerson(request));
    }

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request, Authentication authentication) {
        return AccountResponse.from(toDoService.createAccountForAPerson(request, authentication.getName()));
    }

    @PostMapping("/accounts/{accountId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@Valid @RequestBody CreateTaskRequest request, @PathVariable Long accountId, Authentication authentication) {
        return TaskResponse.from(toDoService.createTask(request, accountId, authentication.getName()));
    }

    @PutMapping("/tasks/{taskId}/priority")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void updateTaskPriority(@PathVariable Long taskId, @Valid @RequestBody UpdateTaskPriorityRequest request) {
        toDoService.updateTaskPriority(taskId, request.priority());
    }

    @PutMapping("/accounts/{accountId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void updateAccountStatus(@PathVariable Long accountId, @Valid @RequestBody UpdateAccountStatusRequest request) {
        toDoService.updateAccountStatus(accountId, request.status());
    }

}
