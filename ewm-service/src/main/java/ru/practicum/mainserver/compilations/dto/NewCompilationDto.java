package ru.practicum.mainserver.compilations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewCompilationDto {

    @NotNull
    private List<Long> events;
    @NotBlank
    private String title;
    @NotNull
    private Boolean pinned;

}
