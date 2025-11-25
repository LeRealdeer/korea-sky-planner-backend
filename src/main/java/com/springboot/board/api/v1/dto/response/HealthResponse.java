package com.springboot.board.api.v1.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@Builder
public class HealthResponse {
  private final String status;
  private final long timestamp;
  private final Details details;

  @Getter
  @AllArgsConstructor
  public static class Details {
    private final long maxMemory;
    private final long freeMemory;
    private final HealthStatus databaseStatus;
  }

  @Getter
  @AllArgsConstructor
  public static class HealthStatus {
    private final String name;
    private final String status;
  }
}