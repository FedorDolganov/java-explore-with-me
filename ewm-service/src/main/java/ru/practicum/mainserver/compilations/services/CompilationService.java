package ru.practicum.mainserver.compilations.services;

import ru.practicum.mainserver.compilations.dto.CompilationDto;

import java.util.List;

public interface CompilationService {
    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilation(long compId);
}
