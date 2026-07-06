package ru.practicum.statsserver.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.statsdto.HitDto;
import ru.practicum.statsdto.StatsDto;
import ru.practicum.statsserver.exceptions.BadRequestException;
import ru.practicum.statsserver.mappers.HitMapper;
import ru.practicum.statsserver.objects.GroupKey;
import ru.practicum.statsserver.objects.Hit;
import ru.practicum.statsserver.repositories.StatsRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class StatsServiceImpl implements StatsService {

    private StatsRepository repository;

    @Transactional
    public ResponseEntity<?> hit(HitDto hitDto) {
        repository.save(HitMapper.to(hitDto));

        return ResponseEntity.status(201).build();
    }

    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, String[] uris, Boolean unique) {
        if (unique == null) {
            unique = false;
        }

        if (start.isAfter(end)) {
            throw new BadRequestException("Начало не может быть позже конца");
        }

        log.info(Arrays.toString(uris));

        Map<GroupKey, List<Hit>> listHits;

        if (uris == null) {
             listHits = repository.findListByTime(start, end).stream()
                     .collect(Collectors.groupingBy(
                             p -> new GroupKey(p.getUri(), p.getApp())
                     ));
        } else {
            listHits = repository.findListByTimeAndListUris(start, end, uris).stream()
                    .collect(Collectors.groupingBy(
                            p -> new GroupKey(p.getUri(), p.getApp())
                    ));
        }

        if (unique) {
            return listHits.entrySet().stream()
                    .map(
                            entry ->
                                    new StatsDto(
                                            entry.getKey().getApp(),
                                            entry.getKey().getUri(),
                                            entry.getValue().stream()
                                                    .map(Hit::getIp)
                                                    .collect(Collectors.toSet())
                                                    .size()
                                    )
                    )
                    .toList();
        } else {
            return listHits.entrySet().stream()
                    .map(
                            entry ->
                                    new StatsDto(
                                            entry.getKey().getApp(),
                                            entry.getKey().getUri(),
                                            entry.getValue().size()
                                    )
                    )
                    .toList();
        }
    }

}
