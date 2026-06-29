package ru.practicum.statsserver.services;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import ru.practicum.statsdto.HitDto;
import ru.practicum.statsdto.StatsDto;
import ru.practicum.statsserver.mappers.HitMapper;
import ru.practicum.statsserver.repositories.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class StatsServiceTest {

    @Autowired
    private StatsServiceImpl service;

    @Autowired
    private StatsRepository repository;

    @BeforeEach
    void setUp() {
        repository.save(HitMapper.to(
                new HitDto(
                        "app",
                        "uri",
                        "ip",
                        LocalDateTime.now()
                )
        ));
    }


    @Test
    void hit() {
        ResponseEntity<?> response = service.hit(
                new HitDto(
                        "app",
                        "uri",
                        "ip",
                        LocalDateTime.now()
                )
        );

        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    void stats() {
        List<StatsDto> list = service.getStats(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                null,
                null
        );

        assertFalse(list.isEmpty());
    }

    @Test
    void stats_WithUris() {
        List<StatsDto> list = service.getStats(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                new String[]{"uri"},
                null
        );

        assertFalse(list.isEmpty());
    }

    @Test
    void stats_WithUnique() {
        List<StatsDto> list = service.getStats(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                null,
                true
        );

        assertFalse(list.isEmpty());
    }
}