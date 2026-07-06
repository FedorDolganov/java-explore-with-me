package ru.practicum.mainserver.admin.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainserver.admin.services.AdminService;
import ru.practicum.mainserver.categories.dto.CategoryDto;
import ru.practicum.mainserver.categories.dto.NewCategoryDto;
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

@RestController
@RequestMapping(path = "/admin")
@AllArgsConstructor
public class AdminController {

    @Autowired
    private AdminService adminService;


    @PostMapping("/categories")
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto categoryDto) {
        return adminService.createCategory(categoryDto);
    }

    @DeleteMapping("/categories/{catId}")
    public void deleteCategory(@PathVariable long catId) {
        adminService.deleteCategory(catId);
    }

    @PatchMapping("/categories/{catId}")
    public CategoryDto updateCategory(@RequestBody NewCategoryDto categoryDto,
                                      @PathVariable long catId) {
        return adminService.updateCategory(catId, categoryDto);
    }


    @GetMapping("/events")
    public List<EventFullDto> getEvents(@RequestParam(required = false) List<Long> users,
                                        @RequestParam(required = false) List<EventState> states,
                                        @RequestParam(required = false) List<Long> categories,
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) LocalDateTime rangeStart,
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) LocalDateTime rangeEnd,
                                        @RequestParam(required = false, defaultValue = "0") Integer from,
                                        @RequestParam(required = false, defaultValue = "10") Integer size) {
        return adminService.getEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/events/{eventId}")
    public EventFullDto updateEvent(@PathVariable long eventId,
                                  @RequestBody UpdateEventAdminRequest eventAdminRequest) {
        return adminService.updateEvent(eventId, eventAdminRequest);
    }


    @GetMapping("/users")
    public List<UserDto> getUsers(@RequestParam(required = false) String[] ids,
                                  @RequestParam(required = false) Integer from,
                                  @RequestParam(required = false) Integer size) {
        return adminService.getUsers(ids, from, size);
    }

    @PostMapping("/users")
    public UserDto createUser(@Valid @RequestBody NewUserRequest userRequest) {
        return adminService.createUser(userRequest);
    }

    @DeleteMapping("/users/{userId}")
    public void deleteUser(@PathVariable long userId) {
        adminService.deleteUser(userId);
    }


    @PostMapping("/compilations")
    public CompilationDto createCompilation(@Valid @RequestBody NewCompilationDto compilationDto) {
        return adminService.createCompilation(compilationDto);
    }

    @DeleteMapping("/compilations/{compId}")
    public void deteleCompilation(@PathVariable long compId) {
        adminService.deteleCompilation(compId);
    }

    @PatchMapping("/compilations/{compId}")
    public CompilationDto updateCompilation(@RequestBody UpdateCompilationRequest compilationRequest,
                                         @PathVariable long compId) {
        return adminService.updateCompilation(compId, compilationRequest);
    }

}
