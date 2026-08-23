package br.com.taskmanager.service;

import br.com.taskmanager.dto.CreateTaskRequest;
import br.com.taskmanager.dto.UpdateTaskRequest;
import br.com.taskmanager.entity.Task;
import br.com.taskmanager.exception.TaskNotFoundException;
import br.com.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

  private final TaskRepository taskRepository;

  public TaskService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  public List<Task> findAll() {
    return taskRepository.findAll();
  }

  public Task create(CreateTaskRequest request) {
    Task task = new Task();

    task.setTitle(request.getTitle());
    task.setDescription(request.getDescription());
    task.setCompleted(false);
    task.setCreatedAt(OffsetDateTime.now());

    return taskRepository.save(task);
  }

  public Task findById(UUID id) {
    return taskRepository.findById(id)
        .orElseThrow(TaskNotFoundException::new);
  }

  public Task update(UUID id, UpdateTaskRequest request) {
    Task task = taskRepository.findById(id)
        .orElseThrow(TaskNotFoundException::new);

    task.setTitle(request.getTitle());
    task.setDescription(request.getDescription());
    task.setCompleted(request.isCompleted());

    return taskRepository.save(task);
  }

  public void delete(UUID id) {
    Task task = taskRepository.findById(id)
        .orElseThrow(TaskNotFoundException::new);

    taskRepository.delete(task);
  }
}