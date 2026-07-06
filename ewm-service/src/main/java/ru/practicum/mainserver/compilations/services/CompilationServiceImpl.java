package ru.practicum.mainserver.compilations.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.mainserver.client.ViewsClient;
import ru.practicum.mainserver.compilations.Compilation;
import ru.practicum.mainserver.compilations.dto.CompilationDto;
import ru.practicum.mainserver.compilations.repositories.CompilationRepository;
import ru.practicum.mainserver.events.Event;
import ru.practicum.mainserver.exceptions.NotFoundException;
import ru.practicum.mainserver.mappers.CompilationMapper;
import ru.practicum.mainserver.users.repositories.ParticipationRequestRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompilationServiceImpl implements CompilationService {

    @Autowired
    private CompilationRepository compilationRepository;

    @Autowired
    private ViewsClient viewsClient;

    @Autowired
    private ParticipationRequestRepository requestRepository;



    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        List<Compilation> compilations;

        if (pinned == null) {
            compilations = compilationRepository.findByFromAndSize(from, size);
        } else {
            compilations = compilationRepository.findByFromAndSizeAndPinned(pinned, from, size);
        }

        List<Long> ids = compilations.stream()
                .flatMap(compilation -> compilation.getEvents().stream().map(Event::getId))
                .toList();

        Map<Long, Integer> requestsCount = requestRepository.getApprovedRequestsCount(ids);
        Map<Long, Integer> viewsCount = viewsClient.getViewsByList(ids);

        return compilations.stream()
                .map(compilation -> CompilationMapper.toDto(
                        compilation,
                        requestsCount,
                        viewsCount
                ))
                .toList();
    }

    @Override
    public CompilationDto getCompilation(long compId) {
        Optional<Compilation> compilation = compilationRepository.findById(compId);

        if (compilation.isEmpty()) {
            throw new NotFoundException(String.format("Compilation with id=%s not found.", compId));
        }

        List<Long> ids = compilation.get().getEvents().stream().map(Event::getId).toList();

        return CompilationMapper.toDto(
                compilation.get(),
                requestRepository.getApprovedRequestsCount(ids),
                viewsClient.getViewsByList(ids)
        );
    }
}
