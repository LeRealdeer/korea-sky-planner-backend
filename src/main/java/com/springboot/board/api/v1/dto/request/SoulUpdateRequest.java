package com.springboot.board.api.v1.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter @NoArgsConstructor
public class SoulUpdateRequest {

    @NotBlank @Size(max = 255) private String seasonName;
    @NotBlank @Size(max = 255) private String name;

    @NotNull  private Integer orderNum;
    @NotNull  private LocalDate startDate;
    @NotNull  private LocalDate endDate;

    private int rerunCount;

    @Size(max = 15) private List<String> keywords;
    @Size(max = 255) private String creator;
    private String description;
}
