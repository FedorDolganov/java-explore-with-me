package ru.practicum.mainserver.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {

    private long id;
    private String text;
    private UserShortDto author;
    private long eventId;
    private LocalDateTime timestamp;

}
