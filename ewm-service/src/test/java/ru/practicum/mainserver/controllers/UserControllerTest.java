package ru.practicum.mainserver.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.mainserver.events.EventState;
import ru.practicum.mainserver.events.EventStateActionUser;
import ru.practicum.mainserver.events.dto.*;
import ru.practicum.mainserver.users.PendingRequestStatus;
import ru.practicum.mainserver.users.UpdateEventRequestStatus;
import ru.practicum.mainserver.users.controllers.UserController;
import ru.practicum.mainserver.users.dto.*;
import ru.practicum.mainserver.users.services.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;


    @Test
    void getUserEvents() throws Exception {
        EventShortDto eventShortDto = new EventShortDto();
        eventShortDto.setId(1L);
        eventShortDto.setTitle("Test event");
        eventShortDto.setAnnotation("Test annotation");

        when(userService.getUserEvents(eq(1L), eq(0), eq(10))).thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/users/{userId}/events", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test event"));

        verify(userService, times(1)).getUserEvents(eq(1L), eq(0), eq(10));
    }

    @Test
    void createEvent() throws Exception {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setTitle("Test titletesttesttest");
        newEventDto.setAnnotation("Test annotationtesttesttest");
        newEventDto.setDescription("Test descriptiontesttesttest");
        newEventDto.setCategory(1L);
        newEventDto.setPaid(false);
        newEventDto.setParticipantLimit(10);
        newEventDto.setRequestModeration(true);
        newEventDto.setLocation(new Location());
        newEventDto.setEventDate(LocalDateTime.of(2020, 1, 1, 10, 0));

        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(1L);
        eventFullDto.setTitle("Test title");
        eventFullDto.setAnnotation("Test annotation");
        eventFullDto.setState(EventState.PENDING);

        when(userService.createEvent(eq(1L), any(NewEventDto.class))).thenReturn(eventFullDto);

        mockMvc.perform(post("/users/{userId}/events", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test title"))
                .andExpect(jsonPath("$.state").value("PENDING"));

        verify(userService, times(1)).createEvent(eq(1L), any(NewEventDto.class));
    }

    @Test
    void getUserEvent() throws Exception {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(1L);
        eventFullDto.setTitle("Test event");
        eventFullDto.setState(EventState.PENDING);

        when(userService.getUserEvent(eq(1L), eq(1L))).thenReturn(eventFullDto);

        mockMvc.perform(get("/users/{userId}/events/{eventId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test event"));

        verify(userService, times(1)).getUserEvent(eq(1L), eq(1L));
    }

    @Test
    void updateUserEvent() throws Exception {
        UpdateEventUserRequest updateRequest = new UpdateEventUserRequest();
        updateRequest.setTitle("Updated eventtesttesttest");
        updateRequest.setAnnotation("Updated annotationtesttesttest");
        updateRequest.setStateAction(EventStateActionUser.SEND_TO_REVIEW);

        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(1L);
        eventFullDto.setTitle("Updated event");
        eventFullDto.setAnnotation("Updated annotation");
        eventFullDto.setState(EventState.PENDING);

        when(userService.updateUserEvent(eq(1L), eq(1L), any(UpdateEventUserRequest.class)))
                .thenReturn(eventFullDto);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated event"))
                .andExpect(jsonPath("$.state").value("PENDING"));

        verify(userService, times(1)).updateUserEvent(eq(1L), eq(1L), any(UpdateEventUserRequest.class));
    }

    @Test
    void getUserEventRequests() throws Exception {
        ParticipationRequestDto requestDto = new ParticipationRequestDto();
        requestDto.setId(1L);
        requestDto.setEvent(1L);
        requestDto.setRequester(1L);
        requestDto.setStatus(PendingRequestStatus.PENDING);

        when(userService.getUserEventRequests(eq(1L), eq(1L))).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].event").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(userService, times(1)).getUserEventRequests(eq(1L), eq(1L));
    }

    @Test
    void updateUserEventRequests() throws Exception {
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L, 2L));
        updateRequest.setStatus(UpdateEventRequestStatus.CONFIRMED);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        ParticipationRequestDto confirmedRequest = new ParticipationRequestDto();
        confirmedRequest.setId(1L);
        confirmedRequest.setStatus(PendingRequestStatus.CONFIRMED);
        result.setConfirmedRequests(List.of(confirmedRequest));

        when(userService.updateUserEventRequests(eq(1L), eq(1L), any(EventRequestStatusUpdateRequest.class)))
                .thenReturn(result);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests[0].id").value(1))
                .andExpect(jsonPath("$.confirmedRequests[0].status").value("CONFIRMED"));

        verify(userService, times(1)).updateUserEventRequests(eq(1L), eq(1L),
                any(EventRequestStatusUpdateRequest.class));
    }


    @Test
    void getUserRequests() throws Exception {
        ParticipationRequestDto requestDto = new ParticipationRequestDto();
        requestDto.setId(1L);
        requestDto.setEvent(2L);
        requestDto.setRequester(1L);
        requestDto.setStatus(PendingRequestStatus.PENDING);

        when(userService.getUserRequests(eq(1L))).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/users/{userId}/requests", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].requester").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(userService, times(1)).getUserRequests(eq(1L));
    }

    @Test
    void createUserEventRequest() throws Exception {
        ParticipationRequestDto requestDto = new ParticipationRequestDto();
        requestDto.setId(1L);
        requestDto.setEvent(2L);
        requestDto.setRequester(1L);
        requestDto.setStatus(PendingRequestStatus.PENDING);

        when(userService.createUserEventRequest(eq(1L), eq(2L))).thenReturn(requestDto);

        mockMvc.perform(post("/users/{userId}/requests", 1L)
                        .param("eventId", String.valueOf(2L)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.event").value(2))
                .andExpect(jsonPath("$.requester").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(userService, times(1)).createUserEventRequest(eq(1L), eq(2L));
    }

    @Test
    void cancelUserRequest() throws Exception {
        ParticipationRequestDto requestDto = new ParticipationRequestDto();
        requestDto.setId(1L);
        requestDto.setEvent(2L);
        requestDto.setRequester(1L);
        requestDto.setStatus(PendingRequestStatus.CANCELED);

        when(userService.cancelUserRequest(eq(1L), eq(1L))).thenReturn(requestDto);

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.requester").value(1))
                .andExpect(jsonPath("$.status").value("CANCELED"));

        verify(userService, times(1)).cancelUserRequest(eq(1L), eq(1L));
    }

    @Test
    void createComment_ShouldReturnCreated() throws Exception {
        NewCommentDto commentDto = new NewCommentDto();
        commentDto.setText("Test comment");

        when(userService.createComment(anyLong(), anyLong(), any(NewCommentDto.class)))
                .thenReturn(new CommentDto());

        mockMvc.perform(post("/users/1/event/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateComment_ShouldReturnOk() throws Exception {
        UpdateCommentDto updateDto = new UpdateCommentDto();
        updateDto.setText("Updated comment");

        when(userService.updateComment(anyLong(), anyLong(), anyLong(), any(UpdateCommentDto.class)))
                .thenReturn(new CommentDto());

        mockMvc.perform(patch("/users/1/event/1/comment/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteComment_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/users/1/event/1/comment/1"))
                .andExpect(status().isNoContent());
    }

}