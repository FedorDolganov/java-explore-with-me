package ru.practicum.statsdto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatsDto {

    private String app;
    private String uri;
    private int hits;

}
