package ch.xenia.todojpa.service;

import ch.xenia.todojpa.domain.TaskElementsFromAi;
import ch.xenia.todojpa.exception.AiServiceException;
import ch.xenia.todojpa.web.dto.request.CreateTaskRequest;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.StructuredChatCompletionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiTaskAnalysisService {
    @Value("${openai.model}")
    private String model;
    private final OpenAIClient client = OpenAIOkHttpClient.fromEnv();

    public AiTaskAnalysisService(){}

    public TaskElementsFromAi analyzeTask(CreateTaskRequest taskRequest) {
        String prompt = """
            Analyze the task.
            Task description:
            %s
            Reply with:
                - Summary: concise one-sentence email-title style summary.
                - Category: select one from the list: PERSONAL, BUSINESS, HEALTH, OTHER; If category is OTHER,
                suggest a category (use two words maximum) or leave it as OTHER.
                - Priority: select one from the list: LOW, MEDIUM, HIGH, CRITICAL.  
        """.formatted(taskRequest.description());

        StructuredChatCompletionCreateParams<TaskElementsFromAi> params = ChatCompletionCreateParams.builder()
                .addUserMessage(prompt)
                .model(model)
                .responseFormat(TaskElementsFromAi.class)
                .build();

        return client.chat()
                .completions()
                .create(params)
                .choices()
                .stream()
                .flatMap(choice -> choice.message().content().stream())
                .findFirst()
                .orElseThrow(() -> new AiServiceException("No data available from " + model));
    }

}
