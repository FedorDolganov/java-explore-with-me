package ru.practicum.statsserver.controllers;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statsdto.HitDto;
import ru.practicum.statsdto.StatsDto;
import ru.practicum.statsserver.services.StatsServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
public class StatsController {

    private StatsServiceImpl service;


    @PostMapping("/hit")
    public ResponseEntity<?> hit(@Valid @RequestBody HitDto hitDto) {
        return service.hit(hitDto);
    }

    @GetMapping("/stats")
    public List<StatsDto> getStats(@RequestParam @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                             @RequestParam @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                             @RequestParam(required = false) String[] uris,
                             @RequestParam(required = false) Boolean unique) {
        return service.getStats(start, end, uris, unique);
    }

}
