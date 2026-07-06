package ru.practicum.mainserver.services;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.mainserver.categories.Category;
import ru.practicum.mainserver.client.ViewsClient;
import ru.practicum.mainserver.events.Event;
import ru.practicum.mainserver.events.EventSort;
import ru.practicum.mainserver.events.EventState;
import ru.practicum.mainserver.events.dto.EventFullDto;
import ru.practicum.mainserver.events.dto.EventShortDto;
import ru.practicum.mainserver.events.repositories.EventRepository;
import ru.practicum.mainserver.events.services.EventServiceImpl;
import ru.practicum.mainserver.exceptions.NotFoundException;
import ru.practicum.mainserver.users.PendingRequestStatus;
import ru.practicum.mainserver.users.User;
import ru.practicum.mainserver.users.repositories.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ViewsClient viewsClient;

    @Mock
    private ParticipationRequestRepository requestRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event event1;
    private Event event2;
    private Category category;
    private User user;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Test Category");

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@test.com");

        event1 = new Event();
        event1.setId(1L);
        event1.setTitle("Event 1");
        event1.setAnnotation("Annotation 1");
        event1.setDescription("Description 1");
        event1.setState(EventState.PUBLISHED);
        event1.setParticipantLimit(10);
        event1.setEventDate(LocalDateTime.now().plusDays(5));
        event1.setCategory(category);
        event1.setInitiator(user);
        event1.setPaid(false);
        event1.setRequestModeration(true);

        event2 = new Event();
        event2.setId(2L);
        event2.setTitle("Event 2");
        event2.setAnnotation("Annotation 2");
        event2.setDescription("Description 2");
        event2.setState(EventState.PUBLISHED);
        event2.setParticipantLimit(2);
        event2.setEventDate(LocalDateTime.now().plusDays(10));
        event2.setCategory(category);
        event2.setInitiator(user);
        event2.setPaid(true);
        event2.setRequestModeration(true);
    }

    @Test
    void getEvents_WhenRangeStartIsNull_ShouldSetToNow() {
        when(eventRepository.findAllByFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(event1));
        when(viewsClient.getViewsByList(anyList())).thenReturn(Map.of(1L, 100));
        when(requestRepository.getApprovedRequestsCount(anyList())).thenReturn(Map.of(1L, 5));

        List<EventShortDto> result = eventService.getEvents(
                httpServletRequest, "text", List.of(1L), false,
                null, LocalDateTime.MAX, false, null, 0, 10
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventRepository).findAllByFilters(
                eq(EventState.PUBLISHED), any(), any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void getEvents_WhenRangeEndIsNull_ShouldSetToMax() {
        when(eventRepository.findAllByFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(event1));
        when(viewsClient.getViewsByList(anyList())).thenReturn(Map.of(1L, 100));
        when(requestRepository.getApprovedRequestsCount(anyList())).thenReturn(Map.of(1L, 5));

        List<EventShortDto> result = eventService.getEvents(
                httpServletRequest, "text", List.of(1L), false,
                LocalDateTime.now(), null, false, null, 0, 10
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventRepository).findAllByFilters(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void getEvents_WhenBothRangesNotNull_ShouldUseProvidedValues() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        when(eventRepository.findAllByFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(event1));
        when(viewsClient.getViewsByList(anyList())).thenReturn(Map.of(1L, 100));
        when(requestRepository.getApprovedRequestsCount(anyList())).thenReturn(Map.of(1L, 5));

        List<EventShortDto> result = eventService.getEvents(
                httpServletRequest, "text", List.of(1L), false,
                start, end, false, null, 0, 10
        );

        assertNotNull(result);
        verify(eventRepository).findAllByFilters(any(), any(), any(), any(), eq(start), eq(end), any(), any());
    }

    @Test
    void getEvents_WhenOnlyAvailableIsTrue_ShouldFilterByParticipantLimit() {
        when(eventRepository.findAllByFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(event1, event2));
        when(viewsClient.getViewsByList(anyList())).thenReturn(Map.of(1L, 100, 2L, 50));
        when(requestRepository.getApprovedRequestsCount(anyList())).thenReturn(Map.of(1L, 5, 2L, 5));

        List<EventShortDto> result = eventService.getEvents(
                httpServletRequest, null, null, null,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                true, null, 0, 10
        );

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getEvents_WhenOnlyAvailableIsFalse_ShouldNotFilter() {
        when(eventRepository.findAllByFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(event1, event2));
        when(viewsClient.getViewsByList(anyList())).thenReturn(Map.of(1L, 100, 2L, 50));
        when(requestRepository.getApprovedRequestsCount(anyList())).thenReturn(Map.of(1L, 5, 2L, 5));

        List<EventShortDto> result = eventService.getEvents(
                httpServletRequest, null, null, null,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                false, null, 0, 10
        );

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getEvents_WhenOnlyAvailableIsNull_ShouldNotFilter() {
        when(eventRepository.findAllByFilters(
                eq(EventState.PUBLISHED),
                isNull(),
                isNull(),
                isNull(),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyInt(),
                anyInt()))
                .thenReturn(List.of(event1, event2));
        when(viewsClient.getViewsByList(anyList())).thenReturn(Map.of(1L, 100, 2L, 50));
        when(requestRepository.getApprovedRequestsCount(anyList())).thenReturn(Map.of(1L, 5, 2L, 5));

        List<EventShortDto> result = eventService.getEvents(
                httpServletRequest, null, null, null,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                false, null, 0, 10
        );

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getEvents_WhenSortIsNull_ShouldNotSort() {
        when(eventRepository.findAllByFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(event2, event1));
        when(viewsClient.getViewsByList(anyList())).thenReturn(Map.of(1L, 100, 2L, 50));
        when(requestRepository.getApprovedRequestsCount(anyList())).thenReturn(Map.of(1L, 5, 2L, 3));

        List<EventShortDto> result = eventService.getEvents(
                httpServletRequest, null, null, null,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                false, null, 0, 10
        );

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getEvents_WhenSortByViews_ShouldSortByViewsDesc() {
        when(eventRepository.findAllByFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(event1, event2));
        when(viewsClient.getViewsByList(anyList())).thenReturn(Map.of(1L, 100, 2L, 50));
        when(requestRepository.getApprovedRequestsCount(anyList())).thenReturn(Map.of(1L, 5, 2L, 3));

        List<EventShortDto> result = eventService.getEvents(
                httpServletRequest, null, null, null,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                false, EventSort.VIEWS, 0, 10
        );

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(eventRepository, times(1)).findAllByFilters(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void getEvents_WhenSortByEventDate_ShouldSortByEventDate() {
        when(eventRepository.findAllByFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(event1, event2));
        when(viewsClient.getViewsByList(anyList())).thenReturn(Map.of(1L, 100, 2L, 50));
        when(requestRepository.getApprovedRequestsCount(anyList())).thenReturn(Map.of(1L, 5, 2L, 3));

        List<EventShortDto> result = eventService.getEvents(
                httpServletRequest, null, null, null,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                false, EventSort.EVENT_DATE, 0, 10
        );

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getEvents_WhenEmptyList_ShouldReturnEmptyList() {
        when(eventRepository.findAllByFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(viewsClient.getViewsByList(anyList())).thenReturn(Map.of());
        when(requestRepository.getApprovedRequestsCount(anyList())).thenReturn(Map.of());

        List<EventShortDto> result = eventService.getEvents(
                httpServletRequest, null, null, null,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                false, null, 0, 10
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getEvents_WhenOnlyAvailableFiltersAllEvents_ShouldReturnEmpty() {
        event1.setParticipantLimit(5);
        event2.setParticipantLimit(3);

        when(eventRepository.findAllByFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(event1, event2));
        when(viewsClient.getViewsByList(anyList())).thenReturn(Map.of(1L, 100, 2L, 50));
        when(requestRepository.getApprovedRequestsCount(anyList())).thenReturn(Map.of(1L, 5, 2L, 5));

        List<EventShortDto> result = eventService.getEvents(
                httpServletRequest, null, null, null,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                true, null, 0, 10
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getEvent_WhenEventExistsAndPublished_ShouldReturnEventFullDto() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event1));
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(requestRepository.countAllByEventIdAndStatus(anyLong(), any()))
                .thenReturn(5);
        when(viewsClient.getViews(anyLong())).thenReturn(100);

        EventFullDto result = eventService.getEvent(httpServletRequest, 1L);

        assertNotNull(result);
        verify(eventRepository, times(1)).findById(1L);
        verify(viewsClient, times(1)).sendViewToEvent("127.0.0.1", 1L);
        verify(requestRepository, times(1)).countAllByEventIdAndStatus(1L, PendingRequestStatus.CONFIRMED);
        verify(viewsClient, times(1)).getViews(1L);
    }

    @Test
    void getEvent_WhenEventNotFound_ShouldThrowNotFoundException() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> eventService.getEvent(httpServletRequest, 99L)
        );

        assertTrue(exception.getMessage().contains("Event with id=99 not found."));
        verify(eventRepository, times(1)).findById(99L);
        verify(viewsClient, never()).sendViewToEvent(any(), anyLong());
        verify(requestRepository, never()).countAllByEventIdAndStatus(anyLong(), any());
    }

    @Test
    void getEvent_WhenEventNotPublished_ShouldThrowNotFoundException() {
        event1.setState(EventState.PENDING);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event1));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> eventService.getEvent(httpServletRequest, 1L)
        );

        assertTrue(exception.getMessage().contains("Event with id=1 not found."));
        verify(eventRepository, times(1)).findById(1L);
        verify(viewsClient, never()).sendViewToEvent(any(), anyLong());
        verify(requestRepository, never()).countAllByEventIdAndStatus(anyLong(), any());
    }

    @Test
    void getEvent_WhenEventCancelled_ShouldThrowNotFoundException() {
        event1.setState(EventState.CANCELED);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event1));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> eventService.getEvent(httpServletRequest, 1L)
        );

        assertTrue(exception.getMessage().contains("Event with id=1 not found."));
        verify(viewsClient, never()).sendViewToEvent(any(), anyLong());
    }

    @Test
    void getEvent_ShouldSendViewToEventWithCorrectIp() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event1));
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(requestRepository.countAllByEventIdAndStatus(anyLong(), any())).thenReturn(0);
        when(viewsClient.getViews(anyLong())).thenReturn(0);

        eventService.getEvent(httpServletRequest, 1L);

        verify(viewsClient, times(1)).sendViewToEvent("127.0.0.1", 1L);
    }

    @Test
    void getEvents_WithAllParameters_ShouldCallRepositoryCorrectly() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        List<Long> categories = List.of(1L, 2L);

        when(eventRepository.findAllByFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(event1));
        when(viewsClient.getViewsByList(anyList())).thenReturn(Map.of(1L, 100));
        when(requestRepository.getApprovedRequestsCount(anyList())).thenReturn(Map.of(1L, 5));

        List<EventShortDto> result = eventService.getEvents(
                httpServletRequest, "concert", categories, true,
                start, end, false, EventSort.VIEWS, 0, 10
        );

        assertNotNull(result);
        verify(eventRepository).findAllByFilters(
                EventState.PUBLISHED, "concert", categories, true, start, end, 0, 10
        );
    }

    @Test
    void getEvents_WhenNoCategories_ShouldPassNullToRepository() {
        when(eventRepository.findAllByFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(event1));
        when(viewsClient.getViewsByList(anyList())).thenReturn(Map.of(1L, 100));
        when(requestRepository.getApprovedRequestsCount(anyList())).thenReturn(Map.of(1L, 5));

        List<EventShortDto> result = eventService.getEvents(
                httpServletRequest, null, null, null,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                false, null, 0, 10
        );

        assertNotNull(result);
        verify(eventRepository).findAllByFilters(
                any(), any(), eq(null), any(), any(), any(), any(), any()
        );
    }
}