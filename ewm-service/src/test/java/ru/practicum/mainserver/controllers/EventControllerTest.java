package ru.practicum.mainserver.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.mainserver.events.EventSort;
import ru.practicum.mainserver.events.EventState;
import ru.practicum.mainserver.events.controllers.EventsControllers;
import ru.practicum.mainserver.events.dto.EventFullDto;
import ru.practicum.mainserver.events.dto.EventShortDto;
import ru.practicum.mainserver.events.services.EventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventsControllers.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void getEvents() throws Exception {
        EventShortDto eventShortDto = new EventShortDto();
        eventShortDto.setId(1L);
        eventShortDto.setTitle("Test title");
        eventShortDto.setPaid(true);

        LocalDateTime rangeStart = LocalDateTime.now().minusDays(1);
        LocalDateTime rangeEnd = LocalDateTime.now().plusDays(1);

        when(eventService.getEvents(
                any(HttpServletRequest.class),
                anyString(),
                anyList(),
                anyBoolean(),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyBoolean(),
                any(EventSort.class),
                anyInt(),
                anyInt()
        )).thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/events")
                        .param("text", "title")
                        .param("categories", "1")
                        .param("paid", "true")
                        .param("rangeStart", rangeStart.format(FORMATTER))
                        .param("rangeEnd", rangeEnd.format(FORMATTER))
                        .param("onlyAvailable", "true")
                        .param("sort", "VIEWS")
                        .param("from", "10")
                        .param("size", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(eventService, times(1)).getEvents(
                any(HttpServletRequest.class),
                anyString(),
                anyList(),
                anyBoolean(),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyBoolean(),
                any(EventSort.class),
                anyInt(),
                anyInt()
        );
    }

    @Test
    void getEvent() throws Exception {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(1L);
        eventFullDto.setTitle("Test event");
        eventFullDto.setAnnotation("Test annotation");
        eventFullDto.setState(EventState.PUBLISHED);

        when(eventService.getEvent(any(HttpServletRequest.class), eq(1L))).thenReturn(eventFullDto);

        mockMvc.perform(get("/events/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test event"))
                .andExpect(jsonPath("$.state").value("PUBLISHED"));

        verify(eventService, times(1)).getEvent(any(HttpServletRequest.class), eq(1L));
    }

}