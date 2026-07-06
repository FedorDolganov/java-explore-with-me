package ru.practicum.mainserver.events.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainserver.events.EventSort;
import ru.practicum.mainserver.events.dto.EventFullDto;
import ru.practicum.mainserver.events.dto.EventShortDto;
import ru.practicum.mainserver.events.services.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController()
@RequestMapping(path = "/events")
@AllArgsConstructor
public class EventsControllers {

    @Autowired
    private EventService eventService;


    @GetMapping
    public List<EventShortDto> getEvents(HttpServletRequest request,
                                   @RequestParam(required = false) String text,
                                   @RequestParam(required = false) List<Long> categories,
                                   @RequestParam(required = false) Boolean paid,
                                   @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) LocalDateTime rangeStart,
                                   @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) LocalDateTime rangeEnd,
                                   @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
                                   @RequestParam(required = false) EventSort sort,
                                   @RequestParam(required = false, defaultValue = "0") Integer from,
                                   @RequestParam(required = false, defaultValue = "10") Integer size) {
        return eventService.getEvents(request, text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(HttpServletRequest request,
                                       @PathVariable long id) {
        return eventService.getEvent(request, id);
    }

}
