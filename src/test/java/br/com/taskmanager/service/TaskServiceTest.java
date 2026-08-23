package br.com.taskmanager.service;

import br.com.taskmanager.dto.CreateTaskRequest;
import br.com.taskmanager.dto.UpdateTaskRequest;
import br.com.taskmanager.entity.Task;
import br.com.taskmanager.exception.TaskNotFoundException;
import br.com.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

  @Mock
  private TaskRepository taskRepository;

  @InjectMocks
  private TaskService taskService;

  private Task task;

  @BeforeEach()
  void setUp() {
    task = buildTask();
  }

  @Nested
  @DisplayName("Ao procurar task pelo id")
  class findByIdTests {

    @Test
    @DisplayName("E o id passado é válido e existe task com ele, então deve ser retornado uma Task com os dados da " +
        "task")
    void findByIdTest_givenAValidIdAndHasTaskWithThisId_shouldReturnTask() {
      when(taskRepository.findById(task.getId()))
          .thenReturn(Optional.of(task));

      Task result = taskService.findById(task.getId());

      assertEquals(task, result);
    }

    @Test
    @DisplayName("E o id passado é válido, mas não existe task com ele, então deve ser retornado uma exceção")
    void findByIdTest_givenValidIdAndHasNoTaskWithThisId_shouldThrowException() {
      UUID id = UUID.randomUUID();

      when(taskRepository.findById(id))
          .thenReturn(Optional.empty());

      assertThrows(
          TaskNotFoundException.class,
          () -> taskService.findById(id)
      );
    }
  }

  @Nested
  @DisplayName("Ao criar uma task")
  class CreateTests {

    @Test
    @DisplayName("E os dados são válidos, então deve criar e salvar uma Task")
    void createTest_givenValidData_shouldCreateAndSaveTask() {

      CreateTaskRequest request = new CreateTaskRequest();
      request.setTitle("Título");
      request.setDescription("Descrição");

      when(taskRepository.save(any(Task.class)))
          .thenReturn(task);

      Task result = taskService.create(request);

      assertEquals(task, result);

      ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);

      verify(taskRepository).save(taskCaptor.capture());

      Task taskSentToRepository = taskCaptor.getValue();

      assertEquals(request.getTitle(), taskSentToRepository.getTitle());
      assertEquals(request.getDescription(), taskSentToRepository.getDescription());
      assertFalse(taskSentToRepository.isCompleted());
    }
  }

  @Nested
  @DisplayName("Ao atualizar uma task")
  class UpdateTests {

    @Test
    @DisplayName("E a task existe, então deve atualizar seus dados")
    void updateTest_givenExistingTaskId_shouldUpdateTask() {
      UpdateTaskRequest request = new UpdateTaskRequest();
      request.setTitle("Título novo");
      request.setDescription("Descrição nova");
      request.setCompleted(true);

      when(taskRepository.findById(task.getId()))
          .thenReturn(Optional.of(task));

      when(taskRepository.save(task))
          .thenReturn(task);

      Task result = taskService.update(task.getId(), request);

      assertEquals("Título novo", result.getTitle());
      assertEquals("Descrição nova", result.getDescription());
      assertTrue(result.isCompleted());

      verify(taskRepository).findById(task.getId());
      verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("E o id passado é válido, mas não existe task com ele, então deve ser retornado uma exceção")
    void updateTest_givenNonExistingTaskId_shouldThrowException() {
      UUID id = UUID.randomUUID();

      UpdateTaskRequest request = new UpdateTaskRequest();
      request.setTitle("Título novo");
      request.setDescription("Descrição nova");
      request.setCompleted(true);

      when(taskRepository.findById(id))
          .thenReturn(Optional.empty());

      assertThrows(
          TaskNotFoundException.class,
          () -> taskService.update(id, request)
      );
    }
  }

  @Nested
  @DisplayName("Ao procurar todas as tasks")
  class FindAllTests {
    @Test
    @DisplayName("E existem tasks cadastradas, então deve retornar todas as tasks")
    void findAllTest_givenExistingTasks_shouldReturnAllTasks() {
      List<Task> tasks = List.of(buildTask(), buildTask());

      when(taskRepository.findAll()).thenReturn(tasks);

      List<Task> result = taskService.findAll();

      assertEquals(tasks, result);
      assertEquals(2, result.size());
      verify(taskRepository).findAll();
    }
  }

  @Nested
  @DisplayName("Ao deletar uma task")
  class DeleteTests {

    @Test
    @DisplayName("E a task existe, então deve ser deletada")
    void deleteTest_givenExistingTaskId_shouldDelete() {

      when(taskRepository.findById(task.getId()))
          .thenReturn(Optional.of(task));

      taskService.delete(task.getId());

      verify(taskRepository).delete(task);
    }


    @Test
    @DisplayName("E a task não existe, então nada deve ser deletado e deve lançar exception")
    void deleteTest_givenNonExistingTaskId_shouldThrowException() {
      UUID id = UUID.randomUUID();

      when(taskRepository.findById(id))
          .thenReturn(Optional.empty());

      assertThrows(
          TaskNotFoundException.class,
          () -> taskService.delete(id)
      );
      verify(taskRepository, never()).delete(any(Task.class));
    }
  }

  private Task buildTask() {
    UUID id = UUID.randomUUID();

    Task task = new Task();
    task.setId(id);
    task.setTitle("Título");
    task.setDescription("Descrição");
    task.setCompleted(false);

    return task;
  }
}