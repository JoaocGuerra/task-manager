package br.com.taskmanager.controller;

import br.com.taskmanager.dto.CreateTaskRequest;
import br.com.taskmanager.dto.UpdateTaskRequest;
import br.com.taskmanager.entity.Task;
import br.com.taskmanager.exception.TaskNotFoundException;
import br.com.taskmanager.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private TaskService taskService;

  private Task buildTask() {
    Task task = new Task();
    task.setId(UUID.randomUUID());
    task.setTitle("Título");
    task.setDescription("Descrição");
    task.setCompleted(false);

    return task;
  }

  @Nested
  @DisplayName("GET /tasks")
  class FindAllTests {

    @Test
    @DisplayName("Deve retornar todas as tasks com status 200")
    void findAll_shouldReturn200() throws Exception {

      Task task1 = buildTask();
      Task task2 = buildTask();

      when(taskService.findAll())
          .thenReturn(List.of(task1, task2));

      mockMvc.perform(get("/tasks"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(2))
          .andExpect(jsonPath("$[0].id").value(task1.getId().toString()))
          .andExpect(jsonPath("$[0].title").value(task1.getTitle()))
          .andExpect(jsonPath("$[1].id").value(task2.getId().toString()))
          .andExpect(jsonPath("$[1].title").value(task2.getTitle()));

      verify(taskService).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia com status 200 quando não existem tasks")
    void findAll_shouldReturnEmptyList() throws Exception {

      when(taskService.findAll())
          .thenReturn(List.of());

      mockMvc.perform(get("/tasks"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(0));

      verify(taskService).findAll();
    }
  }

  @Nested
  @DisplayName("GET /tasks/{id}")
  class FindByIdTests {

    @Test
    @DisplayName("Deve retornar a task com status 200 quando ela existe")
    void findById_givenExistingTask_shouldReturn200() throws Exception {

      Task task = buildTask();

      when(taskService.findById(task.getId()))
          .thenReturn(task);

      mockMvc.perform(get("/tasks/{id}", task.getId()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(task.getId().toString()))
          .andExpect(jsonPath("$.title").value(task.getTitle()))
          .andExpect(jsonPath("$.description").value(task.getDescription()))
          .andExpect(jsonPath("$.completed").value(task.isCompleted()));

      verify(taskService).findById(task.getId());
    }

    @Test
    @DisplayName("Deve retornar 404 quando a task não existe")
    void findById_givenNonExistingTask_shouldReturn404() throws Exception {

      UUID id = UUID.randomUUID();

      when(taskService.findById(id))
          .thenThrow(new TaskNotFoundException());

      mockMvc.perform(get("/tasks/{id}", id))
          .andExpect(status().isNotFound());

      verify(taskService).findById(id);
    }

    @Test
    @DisplayName("Deve retornar 400 quando o id possui formato inválido")
    void findById_givenInvalidId_shouldReturn400() throws Exception {

      mockMvc.perform(get("/tasks/{id}", "id-invalido"))
          .andExpect(status().isBadRequest());

      verify(taskService, never()).findById(any());
    }
  }

  @Nested
  @DisplayName("POST /tasks")
  class CreateTests {

    @Test
    @DisplayName("Deve criar a task e retornar status 201")
    void create_givenValidRequest_shouldReturn201() throws Exception {

      CreateTaskRequest request = new CreateTaskRequest();
      request.setTitle("Nova task");
      request.setDescription("Descrição da task");

      Task createdTask = buildTask();
      createdTask.setTitle(request.getTitle());
      createdTask.setDescription(request.getDescription());

      when(taskService.create(any(CreateTaskRequest.class)))
          .thenReturn(createdTask);

      mockMvc.perform(
              post("/tasks")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(createdTask.getId().toString()))
          .andExpect(jsonPath("$.title").value(request.getTitle()))
          .andExpect(jsonPath("$.description").value(request.getDescription()));

      verify(taskService).create(any(CreateTaskRequest.class));
    }

    @Test
    @DisplayName("Deve retornar 400 quando o JSON é inválido")
    void create_givenInvalidJson_shouldReturn400() throws Exception {

      String invalidJson = """
          {
              "title":
          }
          """;

      mockMvc.perform(
              post("/tasks")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(invalidJson)
          )
          .andExpect(status().isBadRequest());

      verify(taskService, never()).create(any());
    }
  }

  @Nested
  @DisplayName("PUT /tasks/{id}")
  class UpdateTests {

    @Test
    @DisplayName("Deve atualizar a task e retornar status 200")
    void update_givenExistingTask_shouldReturn200() throws Exception {

      UUID id = UUID.randomUUID();

      UpdateTaskRequest request = new UpdateTaskRequest();
      request.setTitle("Título atualizado");
      request.setDescription("Descrição atualizada");
      request.setCompleted(true);

      Task updatedTask = buildTask();
      updatedTask.setId(id);
      updatedTask.setTitle(request.getTitle());
      updatedTask.setDescription(request.getDescription());
      updatedTask.setCompleted(request.isCompleted());

      when(taskService.update(
          any(UUID.class),
          any(UpdateTaskRequest.class)
      )).thenReturn(updatedTask);

      mockMvc.perform(
              put("/tasks/{id}", id)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(id.toString()))
          .andExpect(jsonPath("$.title").value(request.getTitle()))
          .andExpect(jsonPath("$.description").value(request.getDescription()))
          .andExpect(jsonPath("$.completed").value(true));

      verify(taskService).update(
          any(UUID.class),
          any(UpdateTaskRequest.class)
      );
    }

    @Test
    @DisplayName("Deve retornar 404 quando a task não existe")
    void update_givenNonExistingTask_shouldReturn404() throws Exception {

      UUID id = UUID.randomUUID();

      UpdateTaskRequest request = new UpdateTaskRequest();
      request.setTitle("Título atualizado");
      request.setDescription("Descrição atualizada");
      request.setCompleted(true);

      when(taskService.update(
          any(UUID.class),
          any(UpdateTaskRequest.class)
      )).thenThrow(new TaskNotFoundException());

      mockMvc.perform(
              put("/tasks/{id}", id)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isNotFound());

      verify(taskService).update(
          any(UUID.class),
          any(UpdateTaskRequest.class)
      );
    }

    @Test
    @DisplayName("Deve retornar 400 quando o id possui formato inválido")
    void update_givenInvalidId_shouldReturn400() throws Exception {

      UpdateTaskRequest request = new UpdateTaskRequest();
      request.setTitle("Título");
      request.setDescription("Descrição");
      request.setCompleted(true);

      mockMvc.perform(
              put("/tasks/{id}", "id-invalido")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isBadRequest());

      verify(taskService, never()).update(
          any(UUID.class),
          any(UpdateTaskRequest.class)
      );
    }
  }

  @Nested
  @DisplayName("DELETE /tasks/{id}")
  class DeleteTests {

    @Test
    @DisplayName("Deve deletar a task e retornar status 204")
    void delete_givenExistingTask_shouldReturn204() throws Exception {

      UUID id = UUID.randomUUID();

      mockMvc.perform(delete("/tasks/{id}", id))
          .andExpect(status().isNoContent());

      verify(taskService).delete(id);
    }

    @Test
    @DisplayName("Deve retornar 404 quando a task não existe")
    void delete_givenNonExistingTask_shouldReturn404() throws Exception {

      UUID id = UUID.randomUUID();

      doThrow(new TaskNotFoundException())
          .when(taskService)
          .delete(id);

      mockMvc.perform(delete("/tasks/{id}", id))
          .andExpect(status().isNotFound());

      verify(taskService).delete(id);
    }

    @Test
    @DisplayName("Deve retornar 400 quando o id possui formato inválido")
    void delete_givenInvalidId_shouldReturn400() throws Exception {

      mockMvc.perform(delete("/tasks/{id}", "id-invalido"))
          .andExpect(status().isBadRequest());

      verify(taskService, never()).delete(any(UUID.class));
    }
  }
}