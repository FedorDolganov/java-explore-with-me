package ru.practicum.mainserver.compilations.controllers;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainserver.compilations.dto.CompilationDto;
import ru.practicum.mainserver.compilations.services.CompilationService;

import java.util.List;

@RestController
@RequestMapping(path = "/compilations")
@AllArgsConstructor
@Validated
public class CompilationsControllers {

    @Autowired
    private CompilationService compilationService;


    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilation(@Positive @PathVariable long compId) {
        return compilationService.getCompilation(compId);
    }

}
