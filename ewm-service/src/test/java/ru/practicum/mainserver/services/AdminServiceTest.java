package ru.practicum.mainserver.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.mainserver.admin.services.AdminServiceImpl;
import ru.practicum.mainserver.categories.Category;
import ru.practicum.mainserver.categories.dto.CategoryDto;
import ru.practicum.mainserver.categories.dto.NewCategoryDto;
import ru.practicum.mainserver.categories.repositories.CategoryRepository;
import ru.practicum.mainserver.client.ViewsClient;
import ru.practicum.mainserver.compilations.Compilation;
import ru.practicum.mainserver.compilations.dto.CompilationDto;
import ru.practicum.mainserver.compilations.dto.NewCompilationDto;
import ru.practicum.mainserver.compilations.dto.UpdateCompilationRequest;
import ru.practicum.mainserver.compilations.repositories.CompilationRepository;
import ru.practicum.mainserver.events.Event;
import ru.practicum.mainserver.events.EventState;
import ru.practicum.mainserver.events.EventStateAction;
import ru.practicum.mainserver.events.dto.EventFullDto;
import ru.practicum.mainserver.events.dto.UpdateEventAdminRequest;
import ru.practicum.mainserver.events.repositories.EventRepository;
import ru.practicum.mainserver.exceptions.ConflictException;
import ru.practicum.mainserver.exceptions.NotFoundException;
import ru.practicum.mainserver.users.PendingRequestStatus;
import ru.practicum.mainserver.users.User;
import ru.practicum.mainserver.users.dto.NewUserRequest;
import ru.practicum.mainserver.users.dto.UserDto;
import ru.practicum.mainserver.users.repositories.ParticipationRequestRepository;
import ru.practicum.mainserver.users.repositories.UserRepository;
import ru.practicum.mainserver.mappers.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompilationRepository compilationRepository;

    @Mock
    private ViewsClient viewsClient;

    @Mock
    private ParticipationRequestRepository requestRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    private Category category;
    private NewCategoryDto newCategoryDto;
    private Event event;
    private User user;
    private Compilation compilation;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Test Category");

        newCategoryDto = new NewCategoryDto();
        newCategoryDto.setName("Updated Category");

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@test.com");

        event = new Event();
        event.setId(1L);
        event.setTitle("Test Event");
        event.setAnnotation("Test Annotation");
        event.setDescription("Test Description");
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now().minusHours(2));
        event.setInitiator(user);
        event.setCategory(category);
        event.setEventDate(LocalDateTime.now().plusDays(5));
        event.setParticipantLimit(10);
        event.setRequestModeration(true);
        event.setPaid(false);

        compilation = new Compilation();
        compilation.setId(1L);
        compilation.setTitle("Test Compilation");
        compilation.setPinned(true);
        compilation.setEvents(new HashSet<>());
    }



    @Test
    void createCategory_ShouldReturnCategoryDto() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDto result = adminService.createCategory(newCategoryDto);

        assertNotNull(result);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void deleteCategory_ShouldCallRepositoryDelete() {
        adminService.deleteCategory(1L);

        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void updateCategory_WhenCategoryExists_ShouldUpdateAndReturn() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDto result = adminService.updateCategory(1L, newCategoryDto);

        assertNotNull(result);
        assertEquals("Updated Category", category.getName());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void updateCategory_WhenCategoryNotFound_ShouldThrowNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> adminService.updateCategory(99L, newCategoryDto)
        );

        assertTrue(exception.getMessage().contains("Category with id=99 was not found"));
        verify(categoryRepository, never()).save(any());
    }



    @Test
    void getEvents_ShouldReturnListOfEventFullDto() {
        List<Event> events = List.of(event);
        List<Long> ids = List.of(1L);
        Map<Long, Integer> viewsCount = Map.of(1L, 10);
        Map<Long, Integer> requestsCount = Map.of(1L, 5);

        when(eventRepository.findAllByFiltersAdmin(isNull(), isNull(), isNull(),
                isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(events);
        when(viewsClient.getViewsByList(ids)).thenReturn(viewsCount);
        when(requestRepository.getApprovedRequestsCount(ids)).thenReturn(requestsCount);

        List<EventFullDto> result = adminService.getEvents(
                null, null, null, null, null, 0, 10
        );

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void updateEvent_WhenEventNotFound_ShouldThrowNotFoundException() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());
        UpdateEventAdminRequest request = new UpdateEventAdminRequest();

        assertThrows(
                NotFoundException.class,
                () -> adminService.updateEvent(99L, request)
        );
    }

    @Test
    void updateEvent_WhenEventDateTooEarly_ShouldThrowConflictException() {
        event.setCreatedOn(LocalDateTime.now());
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setEventDate(LocalDateTime.now().plusMinutes(30));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminService.updateEvent(1L, request)
        );

        assertTrue(exception.getMessage().contains("must be no earlier than one hour"));
    }

    @Test
    void updateEvent_PublishEvent_WhenStatePending_ShouldPublish() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(requestRepository.countAllByEventIdAndStatus(anyLong(), eq(PendingRequestStatus.CONFIRMED)))
                .thenReturn(5);
        when(viewsClient.getViews(anyLong())).thenReturn(10);

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction(EventStateAction.PUBLISH_EVENT);

        EventFullDto result = adminService.updateEvent(1L, request);

        assertNotNull(result);
        assertEquals(EventState.PUBLISHED, event.getState());
    }

    @Test
    void updateEvent_PublishEvent_WhenStateNotPending_ShouldThrowConflictException() {
        event.setState(EventState.CANCELED);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction(EventStateAction.PUBLISH_EVENT);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminService.updateEvent(1L, request)
        );

        assertTrue(exception.getMessage().contains("Cannot publish the event because it's not in the right state"));
    }

    @Test
    void updateEvent_RejectEvent_WhenStateNotPublished_ShouldCancel() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(requestRepository.countAllByEventIdAndStatus(anyLong(), eq(PendingRequestStatus.CONFIRMED)))
                .thenReturn(0);
        when(viewsClient.getViews(anyLong())).thenReturn(0);

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction(EventStateAction.REJECT_EVENT);

        EventFullDto result = adminService.updateEvent(1L, request);

        assertNotNull(result);
        assertEquals(EventState.CANCELED, event.getState());
    }

    @Test
    void updateEvent_RejectEvent_WhenStatePublished_ShouldThrowConflictException() {
        event.setState(EventState.PUBLISHED);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction(EventStateAction.REJECT_EVENT);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminService.updateEvent(1L, request)
        );

        assertTrue(exception.getMessage().contains("Cannot publish the event because it's not in the right state"));
    }

    @Test
    void updateEvent_WithoutStateAction_ShouldJustUpdate() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(requestRepository.countAllByEventIdAndStatus(anyLong(), eq(PendingRequestStatus.CONFIRMED)))
                .thenReturn(0);
        when(viewsClient.getViews(anyLong())).thenReturn(0);

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();

        EventFullDto result = adminService.updateEvent(1L, request);

        assertNotNull(result);
        assertEquals(EventState.PENDING, event.getState());
    }

    @Test
    void updateEvent_WhenEventDateIsNull_ShouldNotValidateDate() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(requestRepository.countAllByEventIdAndStatus(anyLong(), eq(PendingRequestStatus.CONFIRMED)))
                .thenReturn(0);
        when(viewsClient.getViews(anyLong())).thenReturn(0);

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setEventDate(null);

        EventFullDto result = adminService.updateEvent(1L, request);

        assertNotNull(result);
        verify(eventRepository, times(1)).save(any());
    }



    @Test
    void getUsers_ShouldReturnListOfUserDto() {
        when(userRepository.findAllByIdsAndFromAndSize(any(), any(), any()))
                .thenReturn(List.of(new ru.practicum.mainserver.users.User()));

        List<UserDto> result = adminService.getUsers(new String[]{"1"}, 0, 10);

        assertNotNull(result);
        verify(userRepository, times(1)).findAllByIdsAndFromAndSize(any(), any(), any());
    }

    @Test
    void createUser_ShouldReturnUserDto() {
        NewUserRequest newUserRequest = new NewUserRequest();
        when(userRepository.save(any())).thenReturn(new ru.practicum.mainserver.users.User());

        UserDto result = adminService.createUser(newUserRequest);

        assertNotNull(result);
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void deleteUser_ShouldCallRepositoryDelete() {
        adminService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }



    @Test
    void createCompilation_ShouldReturnCompilationDto() {
        NewCompilationDto newCompilationDto = new NewCompilationDto();
        newCompilationDto.setTitle("Test Compilation");
        newCompilationDto.setPinned(false);
        newCompilationDto.setEvents(List.of(1L, 2L));

        when(eventRepository.findByIds(anyList())).thenReturn(new ArrayList<>());
        when(compilationRepository.save(any(Compilation.class))).thenReturn(compilation);
        when(requestRepository.getApprovedRequestsCount(anyList())).thenReturn(Map.of());
        when(viewsClient.getViewsByList(anyList())).thenReturn(Map.of());

        CompilationDto result = adminService.createCompilation(newCompilationDto);

        assertNotNull(result);
        verify(compilationRepository, times(1)).save(any(Compilation.class));
    }

    @Test
    void deleteCompilation_ShouldCallRepositoryDelete() {
        adminService.deteleCompilation(1L);

        verify(compilationRepository, times(1)).deleteById(1L);
    }

    @Test
    void updateCompilation_WhenCompilationExists_ShouldUpdateAndReturn() {
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setTitle("Updated Compilation");
        request.setPinned(false);

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        when(compilationRepository.save(any(Compilation.class))).thenReturn(compilation);
        when(requestRepository.getApprovedRequestsCount(anyList())).thenReturn(Map.of());
        when(viewsClient.getViewsByList(anyList())).thenReturn(Map.of());

        CompilationDto result = adminService.updateCompilation(1L, request);

        assertNotNull(result);
        verify(compilationRepository, times(1)).save(any(Compilation.class));
    }

    @Test
    void updateCompilation_WhenCompilationNotFound_ShouldThrowNotFoundException() {
        when(compilationRepository.findById(99L)).thenReturn(Optional.empty());
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setTitle("Test");
        request.setPinned(false);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> adminService.updateCompilation(99L, request)
        );

        assertTrue(exception.getMessage().contains("Compilation with id=99 was not found"));
        verify(compilationRepository, never()).save(any());
    }

    @Test
    void getEvents_WithEmptyEventList_ShouldReturnEmptyList() {
        when(eventRepository.findAllByFiltersAdmin(
                eq(List.of(1L)),
                eq(List.of(EventState.PUBLISHED)),
                eq(List.of(1L)),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(0),
                eq(10)))
                .thenReturn(List.of());

        List<EventFullDto> result = adminService.getEvents(
                List.of(1L), List.of(EventState.PUBLISHED), List.of(1L),
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), 0, 10
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}