package ru.practicum.mainserver.events.services;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.mainserver.events.EventSort;
import ru.practicum.mainserver.events.dto.EventFullDto;
import ru.practicum.mainserver.events.dto.EventShortDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventShortDto> getEvents(HttpServletRequest request, String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, EventSort sort, Integer from, Integer size);

    EventFullDto getEvent(HttpServletRequest request, long id);
}
