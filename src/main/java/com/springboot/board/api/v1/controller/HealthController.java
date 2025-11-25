package com.springboot.board.api.v1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.springboot.board.api.v1.dto.response.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@Tag(name = "Health", description = "헬스 체크 API")
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {

  private final JdbcTemplate jdbcTemplate;

  @Operation(summary = "헬스 체크", description = "서버와 데이터베이스의 상태를 확인합니다.")
  @GetMapping
  public HealthResponse checkHealth() {
    HealthResponse.HealthStatus dbStatus = checkDatabase();
    String overallStatus = dbStatus.getStatus().equals("UP") ? "UP" : "DOWN";

    return HealthResponse.builder()
        .status(overallStatus)
        .timestamp(System.currentTimeMillis())
        .details(new HealthResponse.Details(
            Runtime.getRuntime().maxMemory(),
            Runtime.getRuntime().freeMemory(),
            dbStatus))
        .build();
  }

  private HealthResponse.HealthStatus checkDatabase() {
    try {
      jdbcTemplate.queryForObject("SELECT 1", Integer.class);
      return new HealthResponse.HealthStatus("Database", "UP");
    } catch (Exception e) {
      return new HealthResponse.HealthStatus("Database", "DOWN");
    }
  }
}