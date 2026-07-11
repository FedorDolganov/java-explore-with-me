package ru.practicum.mainserver.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.mainserver.categories.Category;
import ru.practicum.mainserver.categories.repositories.CategoryRepository;
import ru.practicum.mainserver.client.ViewsClient;
import ru.practicum.mainserver.events.Event;
import ru.practicum.mainserver.events.EventState;
import ru.practicum.mainserver.events.EventStateActionUser;
import ru.practicum.mainserver.events.dto.EventFullDto;
import ru.practicum.mainserver.events.dto.EventShortDto;
import ru.practicum.mainserver.events.dto.NewEventDto;
import ru.practicum.mainserver.events.dto.UpdateEventUserRequest;
import ru.practicum.mainserver.events.repositories.EventRepository;
import ru.practicum.mainserver.exceptions.BadRequestException;
import ru.practicum.mainserver.exceptions.ConflictException;
import ru.practicum.mainserver.exceptions.ForbiddenException;
import ru.practicum.mainserver.exceptions.NotFoundException;
import ru.practicum.mainserver.users.*;
import ru.practicum.mainserver.users.dto.*;
import ru.practicum.mainserver.users.repositories.CommentRepository;
import ru.practicum.mainserver.users.repositories.ParticipationRequestRepository;
import ru.practicum.mainserver.users.repositories.UserRepository;
import ru.practicum.mainserver.users.services.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ViewsClient viewsClient;

    @Mock
    private ParticipationRequestRepository requestRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private User requester;
    private Event event;
    private Category category;
    private ParticipationRequest request;
    private NewEventDto newEventDto;
    private UpdateEventUserRequest updateEventUserRequest;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Test Category");

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@test.com");

        requester = new User();
        requester.setId(2L);
        requester.setName("Requester");
        requester.setEmail("requester@test.com");

        event = new Event();
        event.setId(1L);
        event.setTitle("Test Event");
        event.setAnnotation("Test Annotation");
        event.setDescription("Test Description");
        event.setState(EventState.PUBLISHED);
        event.setInitiator(user);
        event.setCategory(category);
        event.setParticipantLimit(10);
        event.setRequestModeration(true);
        event.setEventDate(LocalDateTime.now().plusDays(5));
        event.setPaid(false);

        newEventDto = new NewEventDto();
        newEventDto.setEventDate(LocalDateTime.now().plusDays(5));
        newEventDto.setCategory(1L);
        newEventDto.setAnnotation("Test");
        newEventDto.setDescription("Test");

        updateEventUserRequest = new UpdateEventUserRequest();
        updateEventUserRequest.setEventDate(LocalDateTime.now().plusDays(10));
        updateEventUserRequest.setCategory(1L);
        updateEventUserRequest.setAnnotation("Test");
        updateEventUserRequest.setDescription("Test");

        request = new ParticipationRequest();
        request.setId(1L);
        request.setEvent(event);
        request.setRequester(requester);
        request.setStatus(PendingRequestStatus.PENDING);
        request.setCreated(LocalDateTime.now());
    }

    @Test
    void getUserEvents_WhenUserExists_ShouldReturnEventList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findAllByUserIdAndFromAndSize(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(event));
        when(requestRepository.getApprovedRequestsCount(anyList())).thenReturn(Map.of(1L, 5L));
        when(viewsClient.getViewsByList(anyList())).thenReturn(Map.of(1L, 100));

        List<EventShortDto> result = userService.getUserEvents(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).findAllByUserIdAndFromAndSize(1L, 0, 10);
    }

    @Test
    void getUserEvents_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getUserEvents(99L, 0, 10)
        );

        assertTrue(exception.getMessage().contains("User with id=99 not found."));
        verify(eventRepository, never()).findAllByUserIdAndFromAndSize(anyLong(), anyInt(), anyInt());
    }

    @Test
    void getUserEvents_WhenNoEvents_ShouldReturnEmptyList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findAllByUserIdAndFromAndSize(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of());

        List<EventShortDto> result = userService.getUserEvents(1L, 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void createEvent_WhenAllValid_ShouldCreateAndReturnEvent() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(commentRepository.findCommensByEventId(anyLong())).thenReturn(List.of());

        EventFullDto result = userService.createEvent(1L, newEventDto);

        assertNotNull(result);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void createEvent_WhenEventDateBeforeTwoHours_ShouldThrowBadRequestException() {
        newEventDto.setEventDate(LocalDateTime.now().plusHours(1));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> userService.createEvent(1L, newEventDto)
        );

        assertTrue(exception.getMessage().contains("Field: eventDate"));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void createEvent_WhenCategoryNotFound_ShouldThrowNotFoundException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.createEvent(1L, newEventDto)
        );

        assertTrue(exception.getMessage().contains("Category with id=1 not found."));
        verify(userRepository, never()).findById(anyLong());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void createEvent_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.createEvent(1L, newEventDto)
        );

        assertTrue(exception.getMessage().contains("User with id=1 not found."));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void getUserEvent_WhenUserAndEventExist_ShouldReturnEvent() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.countAllByEventIdAndStatus(anyLong(), any())).thenReturn(5);
        when(viewsClient.getViews(anyLong())).thenReturn(100);
        when(commentRepository.findCommensByEventId(anyLong())).thenReturn(List.of());

        EventFullDto result = userService.getUserEvent(1L, 1L);

        assertNotNull(result);
        verify(userRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).findById(1L);
    }

    @Test
    void getUserEvent_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getUserEvent(99L, 1L)
        );

        assertTrue(exception.getMessage().contains("User with id=99 not found."));
        verify(eventRepository, never()).findById(anyLong());
    }

    @Test
    void getUserEvent_WhenEventNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getUserEvent(1L, 99L)
        );

        assertTrue(exception.getMessage().contains("Event with id=99 not found."));
    }

    @Test
    void updateUserEvent_WhenAllValid_CancelReview_ShouldCancelEvent() {
        event.setState(EventState.PENDING);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(requestRepository.countAllByEventIdAndStatus(anyLong(), any())).thenReturn(0);
        when(viewsClient.getViews(anyLong())).thenReturn(0);
        when(commentRepository.findCommensByEventId(anyLong())).thenReturn(List.of());

        updateEventUserRequest.setStateAction(EventStateActionUser.CANCEL_REVIEW);

        EventFullDto result = userService.updateUserEvent(1L, 1L, updateEventUserRequest);

        assertNotNull(result);
        assertEquals(EventState.CANCELED, event.getState());
    }

    @Test
    void updateUserEvent_WhenAllValid_SendToReview_ShouldPendingEvent() {
        event.setState(EventState.CANCELED);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(requestRepository.countAllByEventIdAndStatus(anyLong(), any())).thenReturn(0);
        when(viewsClient.getViews(anyLong())).thenReturn(0);
        when(commentRepository.findCommensByEventId(anyLong())).thenReturn(List.of());

        updateEventUserRequest.setStateAction(EventStateActionUser.SEND_TO_REVIEW);

        EventFullDto result = userService.updateUserEvent(1L, 1L, updateEventUserRequest);

        assertNotNull(result);
        assertEquals(EventState.PENDING, event.getState());
    }

    @Test
    void updateUserEvent_WhenStateActionIsNull_ShouldNotChangeState() {
        event.setState(EventState.PENDING);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(requestRepository.countAllByEventIdAndStatus(anyLong(), any())).thenReturn(0);
        when(viewsClient.getViews(anyLong())).thenReturn(0);
        when(commentRepository.findCommensByEventId(anyLong())).thenReturn(List.of());

        updateEventUserRequest.setStateAction(null);

        EventFullDto result = userService.updateUserEvent(1L, 1L, updateEventUserRequest);

        assertNotNull(result);
        assertEquals(EventState.PENDING, event.getState());
    }

    @Test
    void updateUserEvent_WhenEventNotFound_ShouldThrowNotFoundException() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.updateUserEvent(1L, 99L, updateEventUserRequest)
        );

        assertTrue(exception.getMessage().contains("Event with id=99 not found."));
    }

    @Test
    void updateUserEvent_WhenEventPublished_ShouldThrowConflictException() {
        event.setState(EventState.PUBLISHED);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.updateUserEvent(1L, 1L, updateEventUserRequest)
        );

        assertTrue(exception.getMessage().contains("Only pending or canceled events can be changed"));
    }

    @Test
    void updateUserEvent_WhenUserNotFound_ShouldThrowNotFoundException() {
        event.setState(EventState.PENDING);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> userService.updateUserEvent(1L, 1L, updateEventUserRequest)
        );
    }

    @Test
    void updateUserEvent_WhenCategoryNotFound_ShouldThrowNotFoundException() {
        event.setState(EventState.PENDING);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> userService.updateUserEvent(1L, 1L, updateEventUserRequest)
        );
    }

    @Test
    void getUserEventRequests_WhenUserAndEventExist_ShouldReturnRequests() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.findAllByEventId(1L)).thenReturn(List.of(request));

        List<ParticipationRequestDto> result = userService.getUserEventRequests(1L, 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getUserEventRequests_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getUserEventRequests(99L, 1L)
        );

        assertTrue(exception.getMessage().contains("User with id=99 not found."));
    }

    @Test
    void getUserEventRequests_WhenEventNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getUserEventRequests(1L, 99L)
        );

        assertTrue(exception.getMessage().contains("Event with id=99 not found."));
    }

    @Test
    void getUserEventRequests_WhenNoRequests_ShouldReturnEmptyList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.findAllByEventId(1L)).thenReturn(List.of());

        List<ParticipationRequestDto> result = userService.getUserEventRequests(1L, 1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateUserEventRequests_WhenConfirmed_ShouldConfirmRequests() {
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L));
        updateRequest.setStatus(UpdateEventRequestStatus.CONFIRMED);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findAllById(anyList())).thenReturn(List.of(request));
        when(requestRepository.countAllByEventIdAndStatus(anyLong(), any())).thenReturn(5);
        when(requestRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        EventRequestStatusUpdateResult result = userService.updateUserEventRequests(1L, 1L, updateRequest);

        assertNotNull(result);
        verify(requestRepository, times(2)).saveAll(anyList());
    }

    @Test
    void updateUserEventRequests_WhenRejected_ShouldRejectRequests() {
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L));
        updateRequest.setStatus(UpdateEventRequestStatus.REJECTED);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findAllById(anyList())).thenReturn(List.of(request));
        when(requestRepository.countAllByEventIdAndStatus(anyLong(), any())).thenReturn(5);
        when(requestRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        EventRequestStatusUpdateResult result = userService.updateUserEventRequests(1L, 1L, updateRequest);

        assertNotNull(result);
        verify(requestRepository, times(2)).saveAll(anyList());
    }

    @Test
    void updateUserEventRequests_WhenEventNotFound_ShouldThrowNotFoundException() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.updateUserEventRequests(1L, 99L, updateRequest)
        );

        assertTrue(exception.getMessage().contains("Event with id=99 not found."));
    }

    @Test
    void updateUserEventRequests_WhenEventNotPublished_ShouldThrowForbiddenException() {
        event.setState(EventState.PENDING);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> userService.updateUserEventRequests(1L, 1L, updateRequest)
        );

        assertTrue(exception.getMessage().contains("Event must be published."));
    }

    @Test
    void updateUserEventRequests_WhenNoModeration_ShouldThrowConflictException() {
        event.setState(EventState.PUBLISHED);
        event.setRequestModeration(false);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.updateUserEventRequests(1L, 1L, updateRequest)
        );

        assertTrue(exception.getMessage().contains("Request moderation is disabled"));
    }

    @Test
    void updateUserEventRequests_WhenParticipantLimitIsZero_ShouldThrowConflictException() {
        event.setParticipantLimit(0);
        event.setRequestModeration(true);

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L));

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.updateUserEventRequests(1L, 1L, updateRequest));

        assertTrue(exception.getMessage().contains("Request moderation is disabled or participant limit is 0"));
    }

    @Test
    void updateUserEventRequests_WhenLimitIsZero_ShouldThrowConflictException() {
        event.setParticipantLimit(0);
        event.setRequestModeration(true);

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L));

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.updateUserEventRequests(1L, 1L, updateRequest));

        assertTrue(exception.getMessage().contains("Request moderation is disabled or participant limit is 0"));
    }

    @Test
    void updateUserEventRequests_WhenRequestNotBelongToEvent_ShouldThrowBadRequestException() {
        event.setState(EventState.PUBLISHED);
        Event otherEvent = new Event();
        otherEvent.setId(2L);
        request.setEvent(otherEvent);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findAllById(anyList())).thenReturn(List.of(request));

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> userService.updateUserEventRequests(1L, 1L, updateRequest)
        );

        assertTrue(exception.getMessage().contains("Request does not belong to this event."));
    }

    @Test
    void updateUserEventRequests_WhenRequestNotPending_ShouldThrowConflictException() {
        event.setState(EventState.PUBLISHED);
        request.setStatus(PendingRequestStatus.CONFIRMED);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findAllById(anyList())).thenReturn(List.of(request));

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.updateUserEventRequests(1L, 1L, updateRequest)
        );

        assertTrue(exception.getMessage().contains("Request must have status PENDING."));
    }

    @Test
    void updateUserEventRequests_WhenLimitReached_ShouldThrowConflictException() {
        event.setState(EventState.PUBLISHED);
        event.setParticipantLimit(5);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findAllById(anyList())).thenReturn(List.of(request));
        when(requestRepository.countAllByEventIdAndStatus(anyLong(), any())).thenReturn(5);

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.updateUserEventRequests(1L, 1L, updateRequest)
        );

        assertTrue(exception.getMessage().contains("The participant limit has been reached."));
    }

    @Test
    void getUserRequests_WhenUserExists_ShouldReturnRequests() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findAllByUserId(1L)).thenReturn(List.of(request));

        List<ParticipationRequestDto> result = userService.getUserRequests(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findById(1L);
        verify(requestRepository, times(1)).findAllByUserId(1L);
    }

    @Test
    void getUserRequests_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getUserRequests(99L)
        );

        assertTrue(exception.getMessage().contains("User with id=99 not found."));
        verify(requestRepository, never()).findAllByUserId(anyLong());
    }

    @Test
    void getUserRequests_WhenNoRequests_ShouldReturnEmptyList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findAllByUserId(1L)).thenReturn(List.of());

        List<ParticipationRequestDto> result = userService.getUserRequests(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void createUserEventRequest_WhenAllValid_WithModeration_ShouldCreatePendingRequest() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.findAllByUserIdAndEventId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(requestRepository.countAllByEventIdAndStatus(anyLong(), any())).thenReturn(5);
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(request);

        ParticipationRequestDto result = userService.createUserEventRequest(2L, 1L);

        assertNotNull(result);
        verify(requestRepository, times(1)).save(any(ParticipationRequest.class));
    }

    @Test
    void createUserEventRequest_WhenNoModeration_ShouldAutoConfirm() {
        event.setRequestModeration(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.findAllByUserIdAndEventId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(requestRepository.countAllByEventIdAndStatus(anyLong(), any())).thenReturn(5);
        when(requestRepository.save(any(ParticipationRequest.class))).thenAnswer(invocation -> {
            ParticipationRequest pr = invocation.getArgument(0);
            pr.setStatus(PendingRequestStatus.CONFIRMED);
            return pr;
        });

        ParticipationRequestDto result = userService.createUserEventRequest(2L, 1L);

        assertNotNull(result);
        verify(requestRepository, times(1)).save(any(ParticipationRequest.class));
    }

    @Test
    void createUserEventRequest_WhenLimitIsZero_ShouldAutoConfirm() {
        event.setParticipantLimit(0);
        event.setRequestModeration(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.findAllByUserIdAndEventId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(requestRepository.save(any(ParticipationRequest.class))).thenAnswer(invocation -> invocation.<ParticipationRequest>getArgument(0));

        ParticipationRequestDto result = userService.createUserEventRequest(2L, 1L);

        assertNotNull(result);
        verify(requestRepository, times(1)).save(any(ParticipationRequest.class));
    }

    @Test
    void createUserEventRequest_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.createUserEventRequest(99L, 1L)
        );

        assertTrue(exception.getMessage().contains("User with id=99 not found."));
    }

    @Test
    void createUserEventRequest_WhenEventNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.createUserEventRequest(1L, 99L)
        );

        assertTrue(exception.getMessage().contains("Event with id=99 not found."));
    }

    @Test
    void createUserEventRequest_WhenAlreadyRequested_ShouldThrowConflictException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.findAllByUserIdAndEventId(2L, 1L)).thenReturn(Optional.of(request));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.createUserEventRequest(2L, 1L)
        );

        assertTrue(exception.getMessage().contains("Request for participation has already been sent earlier."));
    }

    @Test
    void createUserEventRequest_WhenInitiatorRequests_ShouldThrowConflictException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.createUserEventRequest(1L, 1L)
        );

        assertTrue(exception.getMessage().contains("You cannot submit a request for your event."));
    }

    @Test
    void createUserEventRequest_WhenEventNotPublished_ShouldThrowConflictException() {
        event.setState(EventState.PENDING);
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.createUserEventRequest(2L, 1L)
        );

        assertTrue(exception.getMessage().contains("You cannot participate in an unpublished event."));
    }

    @Test
    void createUserEventRequest_WhenLimitReached_ShouldThrowConflictException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.findAllByUserIdAndEventId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(requestRepository.countAllByEventIdAndStatus(anyLong(), any())).thenReturn(10);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.createUserEventRequest(2L, 1L)
        );

        assertTrue(exception.getMessage().contains("The participant limit has been reached."));
    }

    @Test
    void cancelUserRequest_WhenAllValid_ShouldCanceledRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(request);

        ParticipationRequestDto result = userService.cancelUserRequest(1L, 1L);

        assertNotNull(result);
        assertEquals(PendingRequestStatus.CANCELED, request.getStatus());
        verify(requestRepository, times(1)).save(request);
    }

    @Test
    void cancelUserRequest_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.cancelUserRequest(99L, 1L)
        );

        assertTrue(exception.getMessage().contains("User with id=99 not found."));
        verify(requestRepository, never()).findById(anyLong());
    }

    @Test
    void cancelUserRequest_WhenRequestNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.cancelUserRequest(1L, 99L)
        );

        assertTrue(exception.getMessage().contains("ParticipationRequest with id=99 not found."));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void createComment_WhenAllValid_ShouldCreateComment() {
        NewCommentDto commentDto = new NewCommentDto();
        commentDto.setText("Test comment");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Test comment");
        comment.setAuthor(requester);
        comment.setEvent(event);

        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.getUserCountAprovedPartocipationToEvent(anyLong(), anyLong(), any())).thenReturn(1);
        when(commentRepository.userHasCommentsToThisEvent(anyLong(), anyLong())).thenReturn(false);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = userService.createComment(2L, 1L, commentDto);

        assertNotNull(result);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void createComment_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.createComment(99L, 1L, new NewCommentDto()));
    }

    @Test
    void createComment_WhenEventNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.createComment(2L, 99L, new NewCommentDto()));
    }

    @Test
    void createComment_WhenUserIsInitiator_ShouldThrowConflictException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.createComment(1L, 1L, new NewCommentDto()));

        assertTrue(exception.getMessage().contains("You cannot comment on your own event"));
    }

    @Test
    void createComment_WhenNotParticipated_ShouldThrowConflictException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.getUserCountAprovedPartocipationToEvent(anyLong(), anyLong(), any())).thenReturn(0);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.createComment(2L, 1L, new NewCommentDto()));

        assertTrue(exception.getMessage().contains("You did not participate in this event"));
    }

    @Test
    void createComment_WhenAlreadyCommented_ShouldThrowConflictException() {
        NewCommentDto commentDto = new NewCommentDto();
        commentDto.setText("Test comment");

        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.getUserCountAprovedPartocipationToEvent(anyLong(), anyLong(), any())).thenReturn(1);
        when(commentRepository.userHasCommentsToThisEvent(anyLong(), anyLong())).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.createComment(2L, 1L, commentDto));

        assertTrue(exception.getMessage().contains("You have already left comments on this event"));
    }

    @Test
    void updateComment_WhenAllValid_ShouldUpdateComment() {
        UpdateCommentDto updateDto = new UpdateCommentDto();
        updateDto.setText("Updated comment");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setEvent(event);
        comment.setAuthor(requester);
        comment.setText("Old comment");

        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = userService.updateComment(2L, 1L, 1L, updateDto);

        assertNotNull(result);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void updateComment_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.updateComment(99L, 1L, 1L, new UpdateCommentDto()));
    }

    @Test
    void updateComment_WhenEventNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.updateComment(2L, 99L, 1L, new UpdateCommentDto()));
    }

    @Test
    void updateComment_WhenCommentNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.updateComment(2L, 1L, 99L, new UpdateCommentDto()));
    }

    @Test
    void updateComment_WhenCommentNotBelongToEvent_ShouldThrowConflictException() {
        Event otherEvent = new Event();
        otherEvent.setId(2L);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setEvent(otherEvent);
        comment.setAuthor(requester);

        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.updateComment(2L, 1L, 1L, new UpdateCommentDto()));

        assertTrue(exception.getMessage().contains("does not belong to the event"));
    }

    @Test
    void updateComment_WhenNotOwner_ShouldThrowConflictException() {
        User otherUser = new User();
        otherUser.setId(3L);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setEvent(event);
        comment.setAuthor(otherUser);

        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.updateComment(2L, 1L, 1L, new UpdateCommentDto()));

        assertTrue(exception.getMessage().contains("You are not the owner"));
    }

    @Test
    void deleteComment_WhenAllValid_ShouldDeleteComment() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setEvent(event);
        comment.setAuthor(requester);

        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        userService.deleteComment(2L, 1L, 1L);

        verify(commentRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteComment_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.deleteComment(99L, 1L, 1L));
    }

    @Test
    void deleteComment_WhenEventNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.deleteComment(2L, 99L, 1L));
    }

    @Test
    void deleteComment_WhenCommentNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.deleteComment(2L, 1L, 99L));
    }

    @Test
    void deleteComment_WhenCommentNotBelongToEvent_ShouldThrowConflictException() {
        Event otherEvent = new Event();
        otherEvent.setId(2L);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setEvent(otherEvent);
        comment.setAuthor(requester);

        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.deleteComment(2L, 1L, 1L));

        assertTrue(exception.getMessage().contains("does not belong to the event"));
    }

    @Test
    void deleteComment_WhenNotOwner_ShouldThrowConflictException() {
        User otherUser = new User();
        otherUser.setId(3L);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setEvent(event);
        comment.setAuthor(otherUser);

        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.deleteComment(2L, 1L, 1L));

        assertTrue(exception.getMessage().contains("You are not the owner"));
    }

    @Test
    void updateUserEvent_WhenEventDateIsNull_ShouldNotValidateDate() {
        event.setState(EventState.PENDING);
        updateEventUserRequest.setEventDate(null);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(requestRepository.countAllByEventIdAndStatus(anyLong(), any())).thenReturn(0);
        when(viewsClient.getViews(anyLong())).thenReturn(0);

        EventFullDto result = userService.updateUserEvent(1L, 1L, updateEventUserRequest);

        assertNotNull(result);
    }

    @Test
    void updateUserEvent_WhenCategoryIsZero_ShouldNotCheckCategory() {
        event.setState(EventState.PENDING);
        updateEventUserRequest.setCategory(0);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(0L)).thenReturn(Optional.empty());
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(requestRepository.countAllByEventIdAndStatus(anyLong(), any())).thenReturn(0);
        when(viewsClient.getViews(anyLong())).thenReturn(0);

        EventFullDto result = userService.updateUserEvent(1L, 1L, updateEventUserRequest);

        assertNotNull(result);
    }

    @Test
    void createUserEventRequest_WhenRequestModerationFalse_ShouldAutoConfirm() {
        event.setRequestModeration(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.findAllByUserIdAndEventId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(requestRepository.countAllByEventIdAndStatus(anyLong(), any())).thenReturn(5);
        when(requestRepository.save(any(ParticipationRequest.class))).thenAnswer(invocation -> {
            ParticipationRequest pr = invocation.getArgument(0);
            pr.setStatus(PendingRequestStatus.CONFIRMED);
            return pr;
        });

        ParticipationRequestDto result = userService.createUserEventRequest(2L, 1L);

        assertNotNull(result);
        verify(requestRepository, times(1)).save(any(ParticipationRequest.class));
    }

    @Test
    void updateUserEvent_WhenEventDateValid_ShouldUpdateEvent() {
        event.setState(EventState.PENDING);
        updateEventUserRequest.setEventDate(LocalDateTime.now().plusDays(5));

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        lenient().when(requestRepository.countAllByEventIdAndStatus(anyLong(), any())).thenReturn(0);
        lenient().when(viewsClient.getViews(anyLong())).thenReturn(0);

        EventFullDto result = userService.updateUserEvent(1L, 1L, updateEventUserRequest);

        assertNotNull(result);
    }

    @Test
    void updateUserEvent_WhenEventDateBeforeTwoHours_ShouldThrowBadRequestException() {
        event.setState(EventState.PENDING);
        updateEventUserRequest.setEventDate(LocalDateTime.now().plusHours(1));

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.updateUserEvent(1L, 1L, updateEventUserRequest));

        assertTrue(exception.getMessage().contains("Field: eventDate"));
    }

    @Test
    void updateUserEventRequests_WhenUserNotFound_ShouldThrowNotFoundException() {
        event.setState(EventState.PUBLISHED);
        event.setParticipantLimit(10);
        event.setRequestModeration(true);

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L));

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.updateUserEventRequests(1L, 1L, updateRequest));

        assertTrue(exception.getMessage().contains("User with id=1 not found"));
    }

    @Test
    void updateUserEventRequests_WhenLimitNotZeroAndFreeRequestsAvailable_ShouldConfirm() {
        event.setState(EventState.PUBLISHED);
        event.setParticipantLimit(10);
        event.setRequestModeration(true);

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L));
        updateRequest.setStatus(UpdateEventRequestStatus.CONFIRMED);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findAllById(anyList())).thenReturn(List.of(request));
        when(requestRepository.countAllByEventIdAndStatus(anyLong(), any())).thenReturn(5);
        when(requestRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        EventRequestStatusUpdateResult result = userService.updateUserEventRequests(1L, 1L, updateRequest);

        assertNotNull(result);
    }
}