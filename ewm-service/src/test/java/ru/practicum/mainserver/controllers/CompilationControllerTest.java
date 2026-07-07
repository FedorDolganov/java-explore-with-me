package ru.practicum.mainserver.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.mainserver.compilations.controllers.CompilationsControllers;
import ru.practicum.mainserver.compilations.dto.CompilationDto;
import ru.practicum.mainserver.compilations.services.CompilationService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompilationsControllers.class)
class CompilationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompilationService compilationService;


    @Test
    void getCompilations() throws Exception {
        when(compilationService.getCompilations(null, 0, 10)).thenReturn(List.of(
                new CompilationDto(
                        1L,
                        true,
                        "Test compilation",
                        null
                )
        ));

        mockMvc.perform(get("/compilations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test compilation"))
                .andExpect(jsonPath("$[0].pinned").value(true));

        verify(compilationService, times(1)).getCompilations(null, 0, 10);
    }

    @Test
    void getCompilation() throws Exception {
        when(compilationService.getCompilation(1L)).thenReturn(
                new CompilationDto(
                        1L,
                        true,
                        "Test compilation",
                        null
                )
        );

        mockMvc.perform(get("/compilations/{compId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test compilation"))
                .andExpect(jsonPath("$.pinned").value(true));

        verify(compilationService, times(1)).getCompilation(1L);
    }

}