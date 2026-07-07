package ru.practicum.mainserver.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.mainserver.categories.Category;
import ru.practicum.mainserver.client.ViewsClient;
import ru.practicum.mainserver.compilations.Compilation;
import ru.practicum.mainserver.compilations.dto.CompilationDto;
import ru.practicum.mainserver.compilations.repositories.CompilationRepository;
import ru.practicum.mainserver.compilations.services.CompilationServiceImpl;
import ru.practicum.mainserver.events.Event;
import ru.practicum.mainserver.events.EventState;
import ru.practicum.mainserver.exceptions.NotFoundException;
import ru.practicum.mainserver.users.User;
import ru.practicum.mainserver.users.repositories.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompilationServiceTest {

    @Mock
    private CompilationRepository compilationRepository;

    @Mock
    private ViewsClient viewsClient;

    @Mock
    private ParticipationRequestRepository requestRepository;

    @InjectMocks
    private CompilationServiceImpl compilationService;

    private Compilation compilation;
    private Compilation compilation2;
    private Event event;
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

        event = new Event();
        event.setId(1L);
        event.setCategory(category);
        event.setInitiator(user);
        event.setState(EventState.PUBLISHED);
        event.setEventDate(LocalDateTime.now().plusDays(5));
        event.setAnnotation("Test");
        event.setDescription("Test");
        event.setParticipantLimit(0);
        event.setPaid(false);

        event2 = new Event();
        event2.setId(2L);
        event2.setCategory(category);
        event2.setInitiator(user);
        event2.setState(EventState.PUBLISHED);
        event2.setEventDate(LocalDateTime.now().plusDays(10));
        event2.setAnnotation("Test");
        event2.setDescription("Test");
        event2.setParticipantLimit(10);
        event2.setPaid(true);

        compilation = new Compilation();
        compilation.setId(1L);
        compilation.setTitle("Test Compilation");
        compilation.setPinned(true);
        compilation.setEvents(new HashSet<>(List.of(event, event2)));


        compilation2 = new Compilation();
        compilation2.setId(2L);
        compilation2.setTitle("Test Compilation 2");
        compilation2.setPinned(false);
        compilation2.setEvents(new HashSet<>(List.of(event)));
    }

    @Test
    void getCompilations_WhenPinnedIsNull_ShouldFindAllWithoutPinnedFilter() {
        when(compilationRepository.findByFromAndSize(anyInt(), anyInt()))
                .thenReturn(List.of(compilation, compilation2));
        when(requestRepository.getApprovedRequestsCount(anyList()))
                .thenReturn(Map.of(1L, 5L, 2L, 3L));
        when(viewsClient.getViewsByList(anyList()))
                .thenReturn(Map.of(1L, 10, 2L, 7));

        List<CompilationDto> result = compilationService.getCompilations(null, 0, 10);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(compilationRepository, times(1)).findByFromAndSize(0, 10);
        verify(compilationRepository, never()).findByFromAndSizeAndPinned(anyBoolean(), anyInt(), anyInt());
        verify(requestRepository, times(1)).getApprovedRequestsCount(anyList());
        verify(viewsClient, times(1)).getViewsByList(anyList());
    }

    @Test
    void getCompilations_WhenPinnedIsTrue_ShouldFindPinnedOnly() {
        when(compilationRepository.findByFromAndSizeAndPinned(anyBoolean(), anyInt(), anyInt()))
                .thenReturn(List.of(compilation));
        when(requestRepository.getApprovedRequestsCount(anyList()))
                .thenReturn(Map.of(1L, 5L, 2L, 3L));
        when(viewsClient.getViewsByList(anyList()))
                .thenReturn(Map.of(1L, 10, 2L, 7));

        List<CompilationDto> result = compilationService.getCompilations(true, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(compilationRepository, times(1)).findByFromAndSizeAndPinned(true, 0, 10);
        verify(compilationRepository, never()).findByFromAndSize(anyInt(), anyInt());
    }

    @Test
    void getCompilations_WhenPinnedIsFalse_ShouldFindNotPinnedOnly() {
        when(compilationRepository.findByFromAndSizeAndPinned(anyBoolean(), anyInt(), anyInt()))
                .thenReturn(List.of(compilation2));
        when(requestRepository.getApprovedRequestsCount(anyList()))
                .thenReturn(Map.of(1L, 5L));
        when(viewsClient.getViewsByList(anyList()))
                .thenReturn(Map.of(1L, 10));

        List<CompilationDto> result = compilationService.getCompilations(false, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(compilationRepository, times(1)).findByFromAndSizeAndPinned(false, 0, 10);
        verify(compilationRepository, never()).findByFromAndSize(anyInt(), anyInt());
    }

    @Test
    void getCompilations_WhenEmptyList_ShouldReturnEmptyList() {
        when(compilationRepository.findByFromAndSize(anyInt(), anyInt()))
                .thenReturn(List.of());
        when(requestRepository.getApprovedRequestsCount(anyList()))
                .thenReturn(Map.of());
        when(viewsClient.getViewsByList(anyList()))
                .thenReturn(Map.of());

        List<CompilationDto> result = compilationService.getCompilations(null, 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCompilations_WhenCompilationHasNoEvents_ShouldHandleEmptyEventList() {
        Compilation emptyCompilation = new Compilation();
        emptyCompilation.setId(3L);
        emptyCompilation.setEvents(new HashSet<>());

        when(compilationRepository.findByFromAndSize(anyInt(), anyInt()))
                .thenReturn(List.of(emptyCompilation));
        when(requestRepository.getApprovedRequestsCount(anyList()))
                .thenReturn(Map.of());
        when(viewsClient.getViewsByList(anyList()))
                .thenReturn(Map.of());

        List<CompilationDto> result = compilationService.getCompilations(null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(requestRepository, times(1)).getApprovedRequestsCount(anyList());
        verify(viewsClient, times(1)).getViewsByList(anyList());
    }

    @Test
    void getCompilations_WithDifferentPagination_ShouldPassCorrectParameters() {
        when(compilationRepository.findByFromAndSize(anyInt(), anyInt()))
                .thenReturn(List.of(compilation));
        when(requestRepository.getApprovedRequestsCount(anyList()))
                .thenReturn(Map.of(1L, 5L, 2L, 3L));
        when(viewsClient.getViewsByList(anyList()))
                .thenReturn(Map.of(1L, 10, 2L, 7));

        List<CompilationDto> result = compilationService.getCompilations(null, 5, 15);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(compilationRepository, times(1)).findByFromAndSize(5, 15);
    }

    @Test
    void getCompilation_WhenCompilationExists_ShouldReturnCompilationDto() {
        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        when(requestRepository.getApprovedRequestsCount(anyList()))
                .thenReturn(Map.of(1L, 5L, 2L, 3L));
        when(viewsClient.getViewsByList(anyList()))
                .thenReturn(Map.of(1L, 10, 2L, 7));

        CompilationDto result = compilationService.getCompilation(1L);

        assertNotNull(result);
        verify(compilationRepository, times(1)).findById(1L);
    }

    @Test
    void getCompilation_WhenCompilationNotFound_ShouldThrowNotFoundException() {
        when(compilationRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> compilationService.getCompilation(99L)
        );

        assertTrue(exception.getMessage().contains("Compilation with id=99 not found."));
        verify(compilationRepository, times(1)).findById(99L);
        verify(requestRepository, never()).getApprovedRequestsCount(any());
        verify(viewsClient, never()).getViewsByList(any());
    }

    @Test
    void getCompilation_WhenCompilationHasNoEvents_ShouldHandleEmptyEventList() {
        Compilation emptyCompilation = new Compilation();
        emptyCompilation.setId(3L);
        emptyCompilation.setEvents(new HashSet<>());

        when(compilationRepository.findById(3L)).thenReturn(Optional.of(emptyCompilation));
        when(requestRepository.getApprovedRequestsCount(anyList()))
                .thenReturn(Map.of());
        when(viewsClient.getViewsByList(anyList()))
                .thenReturn(Map.of());

        CompilationDto result = compilationService.getCompilation(3L);

        assertNotNull(result);
        verify(compilationRepository, times(1)).findById(3L);
        verify(requestRepository, times(1)).getApprovedRequestsCount(List.of());
        verify(viewsClient, times(1)).getViewsByList(List.of());
    }

    @Test
    void getCompilation_WithMultipleEvents_ShouldCollectAllEventIds() {
        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        when(requestRepository.getApprovedRequestsCount(anyList()))
                .thenReturn(Map.of(1L, 5L, 2L, 3L));
        when(viewsClient.getViewsByList(anyList()))
                .thenReturn(Map.of(1L, 10, 2L, 7));

        CompilationDto result = compilationService.getCompilation(1L);

        assertNotNull(result);
        verify(requestRepository, times(1)).getApprovedRequestsCount(argThat(list ->
                list != null && list.size() == 2 && list.contains(1L) && list.contains(2L)
        ));
        verify(viewsClient, times(1)).getViewsByList(argThat(list ->
                list != null && list.size() == 2 && list.contains(1L) && list.contains(2L)
        ));
    }

    @Test
    void getCompilations_ShouldFlattenAllEventIdsCorrectly() {
        when(compilationRepository.findByFromAndSize(anyInt(), anyInt()))
                .thenReturn(List.of(compilation, compilation2));
        when(requestRepository.getApprovedRequestsCount(anyList()))
                .thenReturn(Map.of(1L, 5L, 2L, 3L));
        when(viewsClient.getViewsByList(anyList()))
                .thenReturn(Map.of(1L, 10, 2L, 7));

        List<CompilationDto> result = compilationService.getCompilations(null, 0, 10);

        assertNotNull(result);
        assertEquals(2, result.size());
    }
}