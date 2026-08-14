package ch.xenia.todojpa;

import ch.xenia.todojpa.domain.TaskElementsFromAi;
import ch.xenia.todojpa.web.dto.request.CreateAccountRequest;
import ch.xenia.todojpa.web.dto.request.CreatePersonRequest;
import ch.xenia.todojpa.web.dto.request.CreateTaskRequest;

public class TestDataFactory {

    public static String[] person1 =  {"Xenia@test.com", "Xenia", "Balashova", "Geneva", "Test12345"};
    public static String[] person2 =  {"Xenia1@test.com", "Xenia", "Balashova", "Geneva", "Test56789"};
    public static String account1 = "my professional account";
    public static String account2 = "for private tasks";
    public static String task = "Implement role-based access control in the Spring Boot application using Spring Security. Users should be authenticated with their email and password, while permissions should depend on whether the person has the USER or ADMIN role";

    public TestDataFactory() {
    }

    public static CreatePersonRequest createPersonRequest(String...data) {
        return new CreatePersonRequest(data[0], data[1], data[2], data[3], data[4]);
    }

    public static CreateAccountRequest createAccountRequest(String data) {
        return new CreateAccountRequest(data);
    }

    public static CreateTaskRequest createTaskRequest(String data) {
        return new CreateTaskRequest(data);
    }

    public static TaskElementsFromAi createAiTaskResponse() {
        TaskElementsFromAi aiResponse = new TaskElementsFromAi();
        aiResponse.setSummary("Implement role-based access control");
        aiResponse.setCategory("BUSINESS");
        aiResponse.setAiSuggestedPriority("HIGH");
        return aiResponse;
    }

}
