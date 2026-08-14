package ch.xenia.todojpa.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateTaskRequest(
        @NotBlank
        String description
) {}
