package ru.practicum.mainserver.mappers;

import lombok.experimental.UtilityClass;
import ru.practicum.mainserver.events.Event;
import ru.practicum.mainserver.users.Comment;
import ru.practicum.mainserver.users.User;
import ru.practicum.mainserver.users.dto.CommentDto;
import ru.practicum.mainserver.users.dto.NewCommentDto;

import java.time.LocalDateTime;

@UtilityClass
public class CommentMapper {

    public static CommentDto toDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                UserMapper.toShortDto(comment.getAuthor()),
                comment.getEvent().getId(),
                comment.getTimestamp()
        );
    }

    public static Comment to(NewCommentDto comment, User user, Event event) {
        return new Comment(
                0L,
                comment.getText(),
                event,
                user,
                LocalDateTime.now()
        );
    }

}
