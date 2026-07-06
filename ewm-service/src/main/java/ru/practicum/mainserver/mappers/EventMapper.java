package ru.practicum.mainserver.mappers;

import lombok.experimental.UtilityClass;
import ru.practicum.mainserver.categories.Category;
import ru.practicum.mainserver.events.Event;
import ru.practicum.mainserver.events.EventState;
import ru.practicum.mainserver.events.dto.EventFullDto;
import ru.practicum.mainserver.events.dto.EventShortDto;
import ru.practicum.mainserver.events.dto.NewEventDto;
import ru.practicum.mainserver.events.dto.UpdateEventUserRequest;
import ru.practicum.mainserver.users.User;

import java.time.LocalDateTime;

@UtilityClass
public class EventMapper {

    public static EventShortDto toShortDto(Event event, int confirmedRequests, int views) {
        return new EventShortDto(
                event.getId(),
                event.getTitle(),
                event.getAnnotation(),
                confirmedRequests,
                event.getEventDate(),
                event.isPaid(),
                views,
                CategoryMapper.toDto(event.getCategory()),
                UserMapper.toShortDto(event.getInitiator())
        );
    }

    public static EventFullDto toFullDto(Event event, int confirmedRequests, int views) {
        return new EventFullDto(
                event.getId(),
                event.getTitle(),
                event.getAnnotation(),
                event.getDescription(),
                confirmedRequests,
                event.getCreatedOn(),
                event.getEventDate(),
                event.isPaid(),
                views,
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.isRequestModeration(),
                event.getState(),
                CategoryMapper.toDto(event.getCategory()),
                UserMapper.toShortDto(event.getInitiator()),
                event.getLocation()
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
                eventDto.isPaid(),
                eventDto.getParticipantLimit(),
                eventDto.isRequestModeration(),
                EventState.PENDING
        );
    }

}
