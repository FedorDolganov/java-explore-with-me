package ru.practicum.mainserver.mappers;

import lombok.experimental.UtilityClass;
import ru.practicum.mainserver.compilations.Compilation;
import ru.practicum.mainserver.compilations.dto.CompilationDto;
import ru.practicum.mainserver.compilations.dto.NewCompilationDto;
import ru.practicum.mainserver.compilations.dto.UpdateCompilationRequest;
import ru.practicum.mainserver.events.Event;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@UtilityClass
public class CompilationMapper {


    public static CompilationDto toDto(Compilation compilation, Map<Long, Integer> requestsCount, Map<Long, Integer> viewsCount) {
        return new CompilationDto(
                compilation.getId(),
                compilation.isPinned(),
                compilation.getTitle(),
                compilation.getEvents().stream()
                        .map(event -> EventMapper.toShortDto(
                                    event,
                                    requestsCount.getOrDefault(event.getId(), 0),
                                    viewsCount.getOrDefault(event.getId(), 0)
                                )
                        )
                        .toList()
        );
    }

    public static Compilation to(NewCompilationDto compilationDto, List<Event> events) {
        return new Compilation(
                0L,
                compilationDto.getTitle(),
                compilationDto.getPinned(),
                new HashSet<>(events)
        );
    }

    public static Compilation to(UpdateCompilationRequest compilationDto, long id, Set<Event> events) {
        return new Compilation(
                id,
                compilationDto.getTitle(),
                compilationDto.getPinned(),
                events
        );
    }
}
