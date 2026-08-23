package br.com.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class ErrorResponse {

  private String message;
  private Map<String, String> errors;
}