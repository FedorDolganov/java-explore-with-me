package ru.practicum.statsserver.services;

import org.springframework.http.ResponseEntity;
import ru.practicum.statsdto.HitDto;
import ru.practicum.statsdto.StatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    ResponseEntity<?> hit(HitDto hitDto);

    List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, String[] uris, Boolean unique);

}
