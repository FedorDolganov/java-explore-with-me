package ru.practicum.statsserver.objects;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class GroupKey {

    private String uri;
    private String app;

}
