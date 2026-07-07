package ru.practicum.mainserver.mappers;

import lombok.experimental.UtilityClass;
import ru.practicum.mainserver.users.ParticipationRequest;
import ru.practicum.mainserver.users.User;
import ru.practicum.mainserver.users.dto.NewUserRequest;
import ru.practicum.mainserver.users.dto.ParticipationRequestDto;
import ru.practicum.mainserver.users.dto.UserDto;
import ru.practicum.mainserver.users.dto.UserShortDto;

@UtilityClass
public class UserMapper {

    public static UserShortDto toShortDto(User user) {
        return new UserShortDto(
                user.getId(),
                user.getName()
        );
    }

    public static UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }

    public static ParticipationRequestDto toPRDto(ParticipationRequest request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getCreated(),
                request.getEvent().getId(),
                request.getRequester().getId(),
                request.getStatus()
        );
    }

    public static User to(NewUserRequest userRequest) {
        return new User(
                0L,
                userRequest.getName(),
                userRequest.getEmail()
        );
    }
}
