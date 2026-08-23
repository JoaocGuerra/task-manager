package br.com.taskmanager.dto;

import br.com.taskmanager.entity.Task;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class TaskResponse {

  private final UUID id;
  private final String title;
  private final String description;
  private final boolean completed;
  private final OffsetDateTime createdAt;

  public TaskResponse(Task task) {
    this.id = task.getId();
    this.title = task.getTitle();
    this.description = task.getDescription();
    this.completed = task.isCompleted();
    this.createdAt = task.getCreatedAt();
  }
}