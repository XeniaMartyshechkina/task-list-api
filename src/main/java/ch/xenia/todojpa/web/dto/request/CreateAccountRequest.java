package ch.xenia.todojpa.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequest(
        @NotBlank
        String title
) {}
