package ru.practicum.mainserver.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.mainserver.admin.controllers.AdminController;
import ru.practicum.mainserver.admin.services.AdminService;
import ru.practicum.mainserver.categories.dto.CategoryDto;
import ru.practicum.mainserver.categories.dto.NewCategoryDto;
import ru.practicum.mainserver.compilations.dto.CompilationDto;
import ru.practicum.mainserver.compilations.dto.NewCompilationDto;
import ru.practicum.mainserver.compilations.dto.UpdateCompilationRequest;
import ru.practicum.mainserver.events.EventState;
import ru.practicum.mainserver.events.EventStateAction;
import ru.practicum.mainserver.events.dto.EventFullDto;
import ru.practicum.mainserver.events.dto.Location;
import ru.practicum.mainserver.events.dto.UpdateEventAdminRequest;
import ru.practicum.mainserver.users.dto.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Test
    void createCategory() throws Exception {
        when(adminService.createCategory(any(NewCategoryDto.class))).thenReturn(new CategoryDto(1L, "Test category"));

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new NewCategoryDto("Test category"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test category"));

        verify(adminService, times(1)).createCategory(any(NewCategoryDto.class));
    }

    @Test
    void deleteCategory() throws Exception {
        doNothing().when(adminService).deleteCategory(1L);

        mockMvc.perform(delete("/admin/categories/{catId}", 1L))
                .andExpect(status().isNoContent());

        verify(adminService, times(1)).deleteCategory(1L);
    }

    @Test
    void updateCategory() throws Exception {
        when(adminService.updateCategory(eq(1L), any(NewCategoryDto.class))).thenReturn(new CategoryDto(1L, "Updated category"));

        mockMvc.perform(patch("/admin/categories/{catId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new NewCategoryDto("Updated category"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated category"));

        verify(adminService, times(1)).updateCategory(eq(1L), any(NewCategoryDto.class));
    }

    @Test
    void getEvents() throws Exception {
        when(adminService.getEvents(anyList(), anyList(), anyList(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(new EventFullDto(
                        1L,
                        "Test event",
                        "Test annotation",
                        "",
                        1,
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(1),
                        true,
                        1,
                        1,
                        LocalDateTime.now(),
                        true,
                        EventState.PUBLISHED,
                        new CategoryDto(),
                        new UserShortDto(),
                        new Location(),
                        List.of()
                        )
                )
        );

        mockMvc.perform(get("/admin/events")
                        .param("users", "1", "2")
                        .param("states", "PUBLISHED")
                        .param("categories", "1")
                        .param("rangeStart", LocalDateTime.now().minusDays(1).format(FORMATTER))
                        .param("rangeEnd", LocalDateTime.now().plusDays(1).format(FORMATTER))
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test event"))
                .andExpect(jsonPath("$[0].state").value("PUBLISHED"));

        verify(adminService, times(1)).getEvents(anyList(), anyList(), anyList(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void updateEvent() throws Exception {
        when(adminService.updateEvent(eq(1L), any(UpdateEventAdminRequest.class))).thenReturn(
                new EventFullDto(
                1L,
                "Updated title",
                "Test annotation",
                "",
                1,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                true,
                1,
                1,
                LocalDateTime.now(),
                true,
                EventState.PUBLISHED,
                new CategoryDto(),
                new UserShortDto(),
                new Location(),
                List.of()
        ));

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction(EventStateAction.PUBLISH_EVENT);
        request.setTitle("Updated title");

        mockMvc.perform(patch("/admin/events/{eventId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.state").value("PUBLISHED"));

        verify(adminService, times(1)).updateEvent(eq(1L), any(UpdateEventAdminRequest.class));
    }

    @Test
    void getUsers() throws Exception {
        when(adminService.getUsers(any(String[].class), anyInt(), anyInt()))
                .thenReturn(List.of(new UserDto(
                        1L,
                        "test@gmail.com",
                        "Test user"
                )));

        mockMvc.perform(get("/admin/users")
                        .param("ids", "1", "2")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test user"))
                .andExpect(jsonPath("$[0].email").value("test@gmail.com"));

        verify(adminService, times(1)).getUsers(any(String[].class), anyInt(), anyInt());
    }

    @Test
    void createUser() throws Exception {
        when(adminService.createUser(any(NewUserRequest.class))).thenReturn(new UserDto(
                1L,
                "test@yandex.ru",
                "Updated user"
        ));

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new NewUserRequest(
                                "test@yandex.ru",
                                "Updated user"

                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated user"))
                .andExpect(jsonPath("$.email").value("test@yandex.ru"));

        verify(adminService, times(1)).createUser(any(NewUserRequest.class));
    }

    @Test
    void deleteUser() throws Exception {
        doNothing().when(adminService).deleteUser(1L);

        mockMvc.perform(delete("/admin/users/{userId}", 1L))
                .andExpect(status().isNoContent());

        verify(adminService, times(1)).deleteUser(1L);
    }

    @Test
    void createCompilation() throws Exception {
        when(adminService.createCompilation(any(NewCompilationDto.class))).thenReturn(new CompilationDto(
                1L,
                true,
                "Test compilation",
                null
        ));

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new NewCompilationDto(
                                List.of(1L, 2L),
                                "Test compilation",
                                true
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test compilation"))
                .andExpect(jsonPath("$.pinned").value(true));

        verify(adminService, times(1)).createCompilation(any(NewCompilationDto.class));
    }

    @Test
    void deleteCompilation() throws Exception {
        doNothing().when(adminService).deteleCompilation(1L);

        mockMvc.perform(delete("/admin/compilations/{compId}", 1L))
                .andExpect(status().isNoContent());

        verify(adminService, times(1)).deteleCompilation(1L);
    }

    @Test
    void updateCompilation() throws Exception {
        UpdateCompilationRequest updateRequest = new UpdateCompilationRequest();
        updateRequest.setTitle("Updated compilation");
        updateRequest.setPinned(false);
        updateRequest.setEvents(List.of(1L, 3L));

        when(adminService.updateCompilation(eq(1L), any(UpdateCompilationRequest.class)))
                .thenReturn(new CompilationDto(
                        1L,
                        false,
                        "Updated compilation",
                        null
                ));

        mockMvc.perform(patch("/admin/compilations/{compId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated compilation"))
                .andExpect(jsonPath("$.pinned").value(false));

        verify(adminService, times(1)).updateCompilation(eq(1L), any(UpdateCompilationRequest.class));
    }

    @Test
    void updateComment_ShouldReturnOk() throws Exception {
        UpdateCommentDto updateDto = new UpdateCommentDto();
        updateDto.setText("Updated comment");

        when(adminService.updateComments(anyLong(), any(UpdateCommentDto.class)))
                .thenReturn(new CommentDto());

        mockMvc.perform(patch("/admin/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteComment_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/admin/comments/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getComments_ShouldReturnOk() throws Exception {
        when(adminService.getComments(any(String[].class), anyInt(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/admin/comments")
                        .param("ids", "1,2")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

}