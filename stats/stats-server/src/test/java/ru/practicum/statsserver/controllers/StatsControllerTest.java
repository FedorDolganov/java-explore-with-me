package ru.practicum.statsserver.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.statsdto.HitDto;
import ru.practicum.statsdto.StatsDto;
import ru.practicum.statsserver.services.StatsServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
public class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StatsServiceImpl service;


    @Test
    void post_Hit() throws Exception {

        when(service.hit(any(HitDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/hit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            new HitDto(
                                    "app",
                                    "uri",
                                    "ip",
                                    LocalDateTime.now()
                            )
                    )))
                .andExpect(status().isOk());

        verify(service, times(1)).hit(any(HitDto.class));
    }

    @Test
    void get_stats() throws Exception {
        when(service.getStats(any(LocalDateTime.class), any(LocalDateTime.class), any(), anyBoolean()))
                .thenReturn(List.of(new StatsDto("uri", "app", 2)));

        mockMvc.perform(get("/stats")
                        .param("start", "2000-01-01T00:00:00")
                        .param("end", "2000-01-01T10:00:00")
                        .param("uris", "/test")
                        .param("unique", "false"))
                .andExpect(status().isOk());

        verify(service, times(1)).getStats(any(LocalDateTime.class), any(LocalDateTime.class), any(), anyBoolean());
    }

}