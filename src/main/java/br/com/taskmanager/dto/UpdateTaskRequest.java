package br.com.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTaskRequest {

  @NotBlank
  private String title;

  private String description;

  private boolean completed;
}