package ru.practicum.mainserver.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.mainserver.categories.dto.CategoryDto;
import ru.practicum.mainserver.users.dto.CommentDto;
import ru.practicum.mainserver.events.EventState;
import ru.practicum.mainserver.users.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventFullDto {

    private long id;
    private String title;
    private String annotation;
    private String description;
    private long confirmedRequests;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private boolean paid;
    private int views;
    private int participantLimit;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;
    private boolean requestModeration;
    private EventState state;
    private CategoryDto category;
    private UserShortDto initiator;
    private Location location;
    private List<CommentDto> comments;

}
