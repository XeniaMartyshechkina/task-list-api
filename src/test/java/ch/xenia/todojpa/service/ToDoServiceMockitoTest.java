package ch.xenia.todojpa.service;

import ch.xenia.todojpa.TestDataFactory;
import ch.xenia.todojpa.domain.*;
import ch.xenia.todojpa.web.dto.request.CreateTaskRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ToDoServiceMockitoTest{

    @Mock
    private EntityManager entityManager;

    @Mock
    private AiTaskAnalysisService aiTaskAnalysisService;

    @Mock
    private TaskPersistenceService taskPersistenceService;

    @InjectMocks
    private ToDoPersistenceService toDoService;

    @Test
    void shouldCreateTask() {

        Long accountId = 1L;
        String email = "Xenia@test.com";

        CreateTaskRequest request =
                TestDataFactory.createTaskRequest(
                        TestDataFactory.task
                );

        TaskElementsFromAi aiResponse =
                TestDataFactory.createAiTaskResponse();

        when(aiTaskAnalysisService.analyzeTask(request))
                .thenReturn(aiResponse);

        when(taskPersistenceService.persistTask(
                any(Task.class),
                eq(accountId),
                eq(email)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task task = toDoService.createTask(request, accountId, email);

        assertNotNull(task);
        assertEquals(TestDataFactory.task, task.getDescription());
        assertEquals(
                aiResponse.getSummary(),
                task.getSummary()
        );
        assertEquals(TaskStatusEnum.TO_DO, task.getStatus());
        assertEquals(TaskCategoryEnum.BUSINESS, task.getCategory());
        assertEquals(TaskPriorityEnum.HIGH, task.getPriority());

        verify(aiTaskAnalysisService)
                .analyzeTask(request);

        verify(taskPersistenceService)
                .persistTask(
                        task,
                        accountId,
                        email
                );
    }

}
