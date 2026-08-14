package ch.xenia.todojpa.web.dto.request;

import ch.xenia.todojpa.domain.AccountStatusEnum;
import jakarta.validation.constraints.NotNull;

public record UpdateAccountStatusRequest(
        @NotNull
        AccountStatusEnum status
) {}
