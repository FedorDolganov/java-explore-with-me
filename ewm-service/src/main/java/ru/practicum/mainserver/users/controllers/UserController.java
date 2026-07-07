package ru.practicum.mainserver.users.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainserver.events.dto.EventFullDto;
import ru.practicum.mainserver.events.dto.EventShortDto;
import ru.practicum.mainserver.events.dto.NewEventDto;
import ru.practicum.mainserver.events.dto.UpdateEventUserRequest;
import ru.practicum.mainserver.users.dto.EventRequestStatusUpdateRequest;
import ru.practicum.mainserver.users.dto.EventRequestStatusUpdateResult;
import ru.practicum.mainserver.users.dto.ParticipationRequestDto;
import ru.practicum.mainserver.users.services.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@AllArgsConstructor
@Validated
public class UserController {

    @Autowired
    private UserService userService;



    @GetMapping("/{userId}/events")
    public List<EventShortDto> getUserEvents(@Positive @PathVariable long userId,
                                             @RequestParam(defaultValue = "0") Integer from,
                                             @RequestParam(defaultValue = "10") Integer size) {
        return userService.getUserEvents(userId, from, size);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{userId}/events")
    public EventFullDto createEvent(@Positive @PathVariable long userId,
                                     @Valid  @RequestBody NewEventDto eventDto) {
        return userService.createEvent(userId, eventDto);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getUserEvent(@Positive @PathVariable long userId,
                                     @Positive @PathVariable long eventId) {
        return userService.getUserEvent(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto updateUserEvent(@Positive @PathVariable long userId,
                                        @Positive @PathVariable long eventId,
                                        @Valid @RequestBody UpdateEventUserRequest eventUserRequest) {
        return userService.updateUserEvent(userId, eventId, eventUserRequest);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getUserEventRequests(@Positive @PathVariable long userId,
                                                              @Positive @PathVariable long eventId) {
        return userService.getUserEventRequests(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateUserEventRequests(@Positive @PathVariable long userId,
                                                                  @Positive @PathVariable long eventId,
                                                                  @Valid @RequestBody EventRequestStatusUpdateRequest eventRequest) {
        return userService.updateUserEventRequests(userId, eventId, eventRequest);
    }


    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> getUserRequests(@Positive @PathVariable long userId) {
        return userService.getUserRequests(userId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{userId}/requests")
    public ParticipationRequestDto createUserEventRequest(@Positive @PathVariable long userId,
                                                          @Positive @RequestParam long eventId) {
        return userService.createUserEventRequest(userId, eventId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelUserRequest(@Positive @PathVariable long userId,
                                                     @Positive @PathVariable long requestId) {
        return userService.cancelUserRequest(userId, requestId);
    }

}
