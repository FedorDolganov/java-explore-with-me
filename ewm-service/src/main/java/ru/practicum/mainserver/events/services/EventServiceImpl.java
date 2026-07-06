package ru.practicum.mainserver.events.services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.mainserver.client.ViewsClient;
import ru.practicum.mainserver.events.Event;
import ru.practicum.mainserver.events.EventSort;
import ru.practicum.mainserver.events.EventState;
import ru.practicum.mainserver.events.dto.EventFullDto;
import ru.practicum.mainserver.events.dto.EventShortDto;
import ru.practicum.mainserver.events.repositories.EventRepository;
import ru.practicum.mainserver.exceptions.NotFoundException;
import ru.practicum.mainserver.mappers.EventMapper;
import ru.practicum.mainserver.users.PendingRequestStatus;
import ru.practicum.mainserver.users.repositories.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ViewsClient viewsClient;

    @Autowired
    private ParticipationRequestRepository requestRepository;



    @Override
    public List<EventShortDto> getEvents(HttpServletRequest request, String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, EventSort sort, Integer from, Integer size) {
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }

        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.MAX;
        }

        List<Event> events = eventRepository.findAllByFilters(EventState.PUBLISHED, text, categories, paid, rangeStart, rangeEnd, from, size);

        List<Long> eventIds = events.stream().map(Event::getId).toList();

        Map<Long, Integer> viewsCount = viewsClient.getViewsByList(eventIds);

        Map<Long, Integer> requestsCount = requestRepository.getApprovedRequestsCount(eventIds);

        if (onlyAvailable) {
            events = events.stream()
                    .filter(event -> event.getParticipantLimit() - requestsCount.get(event.getId()) > 0)
                    .toList();
        }

        List<EventShortDto> eventsDto = events.stream()
                .map(event -> EventMapper.toShortDto(
                        event,
                        requestsCount.get(event.getId()),
                        viewsCount.get(event.getId())
                ))
                .toList();

        if (sort != null) {
            switch (sort) {
                case VIEWS:
                    eventsDto = eventsDto.stream()
                        .sorted(Comparator.comparingInt(EventShortDto::getViews).reversed())
                        .toList();
                    break;
                case EVENT_DATE:
                    eventsDto = eventsDto.stream()
                            .sorted(Comparator.comparing(EventShortDto::getEventDate))
                            .toList();
                    break;
            }
        }

        return eventsDto;
    }

    @Override
    public EventFullDto getEvent(HttpServletRequest request, long id) {
        Optional<Event> event = eventRepository.findById(id);

        if (event.isEmpty()) {
            throw new NotFoundException(String.format("Event with id=%s not found.", id));
        }

        if (event.get().getState() != EventState.PUBLISHED) {
            throw new NotFoundException(String.format("Event with id=%s not found.", id));
        }

        viewsClient.sendViewToEvent(request.getRemoteAddr(), id);

        return EventMapper.toFullDto(
                event.get(),
                requestRepository.countAllByEventIdAndStatus(id, PendingRequestStatus.CONFIRMED),
                viewsClient.getViews(id)
        );
    }
}
