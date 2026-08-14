package ch.xenia.todojpa.web.dto.response;

import ch.xenia.todojpa.domain.Account;
import ch.xenia.todojpa.domain.AccountStatusEnum;

import java.util.List;

public record AccountResponse(
        Long id,
        String title,
        AccountStatusEnum status,
        List<TaskResponse> tasks
) {
    public static AccountResponse from(Account account) {
        List<TaskResponse> tasksDto = account.getTasks() == null
                ? List.of()
                : account.getTasks().stream().map(TaskResponse::from).toList();
        return new AccountResponse(
                account.getId(),
                account.getTitle(),
                account.getStatus(),
                tasksDto);
    }
}
