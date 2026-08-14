package ch.xenia.todojpa.web.dto.response;

import ch.xenia.todojpa.domain.Task;
import ch.xenia.todojpa.domain.TaskCategoryEnum;
import ch.xenia.todojpa.domain.TaskPriorityEnum;
import ch.xenia.todojpa.domain.TaskStatusEnum;

public record TaskResponse(
        Long id,
        String summary,
        String description,
        TaskStatusEnum status,
        TaskCategoryEnum category,
        TaskPriorityEnum priority

) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getSummary(),
                task.getDescription(),
                task.getStatus(),
                task.getCategory(),
                task.getPriority()
        );
    }
}
