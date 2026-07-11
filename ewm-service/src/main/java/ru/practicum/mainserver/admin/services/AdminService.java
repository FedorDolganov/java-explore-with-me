package ru.practicum.mainserver.admin.services;

import jakarta.validation.Valid;
import ru.practicum.mainserver.categories.dto.CategoryDto;
import ru.practicum.mainserver.categories.dto.NewCategoryDto;
import ru.practicum.mainserver.users.dto.CommentDto;
import ru.practicum.mainserver.users.dto.UpdateCommentDto;
import ru.practicum.mainserver.compilations.dto.CompilationDto;
import ru.practicum.mainserver.compilations.dto.NewCompilationDto;
import ru.practicum.mainserver.compilations.dto.UpdateCompilationRequest;
import ru.practicum.mainserver.events.EventState;
import ru.practicum.mainserver.events.dto.EventFullDto;
import ru.practicum.mainserver.events.dto.UpdateEventAdminRequest;
import ru.practicum.mainserver.users.dto.NewUserRequest;
import ru.practicum.mainserver.users.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminService {
    CategoryDto createCategory(NewCategoryDto categoryDto);

    void deleteCategory(long catId);

    CategoryDto updateCategory(long catId, NewCategoryDto categoryDto);

    List<EventFullDto> getEvents(List<Long> users, List<EventState> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullDto updateEvent(long eventId, UpdateEventAdminRequest eventAdminRequest);

    List<UserDto> getUsers(String[] ids, Integer from, Integer size);

    UserDto createUser(NewUserRequest userRequest);

    void deleteUser(long userId);

    CompilationDto createCompilation(@Valid NewCompilationDto compilationDto);

    void deteleCompilation(long compId);

    CompilationDto updateCompilation(long compId, UpdateCompilationRequest compilationRequest);

    CommentDto updateComments(long comId, @Valid UpdateCommentDto commentDto);

    List<CommentDto> getComments(String[] ids, Integer from, Integer size);

    void deleteComments(long comId);
}
