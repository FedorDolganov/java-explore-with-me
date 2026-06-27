package ru.practicum.statsserver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.statsserver.objects.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {

    @Query("select h " +
            "from Hit h " +
            "where h.timestamp > ?1 " +
            "and h.timestamp < ?2")
    List<Hit> findListByTime(LocalDateTime start, LocalDateTime end);

    @Query("select h " +
            "from Hit h " +
            "where h.timestamp > ?1 " +
            "and h.timestamp < ?2 " +
            "and h.uri in ?3")
    List<Hit> findListByTimeAndListUris(LocalDateTime start, LocalDateTime end, String[] uris);

}
