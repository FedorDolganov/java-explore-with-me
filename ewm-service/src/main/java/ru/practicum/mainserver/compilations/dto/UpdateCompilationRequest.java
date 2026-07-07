package ru.practicum.mainserver.compilations.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompilationRequest {

    private List<Long> events = new ArrayList<>();
    @Length(min = 1, max = 50)
    private String title;
    private Boolean pinned = false;

}
