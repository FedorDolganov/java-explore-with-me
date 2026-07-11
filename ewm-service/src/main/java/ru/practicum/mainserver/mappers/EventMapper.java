package ru.practicum.mainserver.mappers;

import lombok.experimental.UtilityClass;
import ru.practicum.mainserver.categories.Category;
import ru.practicum.mainserver.users.dto.CommentDto;
import ru.practicum.mainserver.events.Event;
import ru.practicum.mainserver.events.EventState;
import ru.practicum.mainserver.events.dto.*;
import ru.practicum.mainserver.users.User;

import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
public class EventMapper {

    public static EventShortDto toShortDto(Event event, long confirmedRequests, int views) {
        return new EventShortDto(
                event.getId(),
                event.getTitle(),
                event.getAnnotation(),
                confirmedRequests,
                event.getEventDate(),
                event.getPaid(),
                views,
                CategoryMapper.toDto(event.getCategory()),
                UserMapper.toShortDto(event.getInitiator())
        );
    }

    public static EventFullDto toFullDto(Event event, long confirmedRequests, int views, List<CommentDto> comments) {
        return new EventFullDto(
                event.getId(),
                event.getTitle(),
                event.getAnnotation(),
                event.getDescription(),
                confirmedRequests,
                event.getCreatedOn(),
                event.getEventDate(),
                event.getPaid(),
                views,
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.getRequestModeration(),
                event.getState(),
                CategoryMapper.toDto(event.getCategory()),
                UserMapper.toShortDto(event.getInitiator()),
                event.getLocation(),
                comments
        );
    }

    public static Event toEvent(NewEventDto eventDto, Category category, User initiator) {
        return new Event(
                0L,
                eventDto.getTitle(),
                eventDto.getAnnotation(),
                eventDto.getDescription(),
                category,
                initiator,
                eventDto.getLocation(),
                eventDto.getEventDate(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                eventDto.isPaid(),
                eventDto.getParticipantLimit(),
                eventDto.isRequestModeration(),
                EventState.PENDING
        );
    }

    public static Event toEvent(UpdateEventUserRequest eventDto, Category category, User initiator) {
        return new Event(
                0L,
                eventDto.getTitle(),
                eventDto.getAnnotation(),
                eventDto.getDescription(),
                category,
                initiator,
                eventDto.getLocation(),
                eventDto.getEventDate(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                eventDto.getPaid(),
                eventDto.getParticipantLimit(),
                eventDto.getRequestModeration(),
                EventState.PENDING
        );
    }

    public static Event toEvent(UpdateEventAdminRequest eventDto, Category category) {
        return new Event(
                0L,
                eventDto.getTitle(),
                eventDto.getAnnotation(),
                eventDto.getDescription(),
                category,
                null,
                eventDto.getLocation(),
                eventDto.getEventDate(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                eventDto.getPaid(),
                eventDto.getParticipantLimit(),
                eventDto.getRequestModeration(),
                null
        );
    }

}
