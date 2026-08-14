package ch.xenia.todojpa.web.dto.request;

import ch.xenia.todojpa.domain.TaskPriorityEnum;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskPriorityRequest(
        @NotNull
        TaskPriorityEnum priority
) {}
