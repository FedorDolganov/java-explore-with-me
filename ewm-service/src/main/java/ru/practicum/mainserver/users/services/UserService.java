package ru.practicum.mainserver.users.services;

import jakarta.validation.Valid;
import ru.practicum.mainserver.events.dto.EventFullDto;
import ru.practicum.mainserver.events.dto.EventShortDto;
import ru.practicum.mainserver.events.dto.NewEventDto;
import ru.practicum.mainserver.events.dto.UpdateEventUserRequest;
import ru.practicum.mainserver.users.dto.EventRequestStatusUpdateRequest;
import ru.practicum.mainserver.users.dto.EventRequestStatusUpdateResult;
import ru.practicum.mainserver.users.dto.ParticipationRequestDto;

import java.util.List;

public interface UserService {
    List<EventShortDto> getUserEvents(long userId, Integer from, Integer size);

    EventFullDto createEvent(long userId, @Valid NewEventDto eventDto);

    EventFullDto getUserEvent(long userId, long eventId);

    EventFullDto updateUserEvent(long userId, long eventId, UpdateEventUserRequest eventUserRequest);

    List<ParticipationRequestDto> getUserEventRequests(long userId, long eventId);

    EventRequestStatusUpdateResult updateUserEventRequests(long userId, long eventId, EventRequestStatusUpdateRequest eventRequest);

    List<ParticipationRequestDto> getUserRequests(long userId);

    ParticipationRequestDto createUserEventRequest(long userId, long eventId);

    ParticipationRequestDto cancelUserRequest(long userId, long requestId);
}
