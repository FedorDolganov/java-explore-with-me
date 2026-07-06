package ru.practicum.mainserver.users.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
import ru.practicum.mainserver.mappers.EventMapper;
import ru.practicum.mainserver.mappers.UpdateMapper;
import ru.practicum.mainserver.mappers.UserMapper;
import ru.practicum.mainserver.users.ParticipationRequest;
import ru.practicum.mainserver.users.PendingRequestStatus;
import ru.practicum.mainserver.users.UpdateEventRequestStatus;
import ru.practicum.mainserver.users.User;
import ru.practicum.mainserver.users.dto.EventRequestStatusUpdateRequest;
import ru.practicum.mainserver.users.dto.EventRequestStatusUpdateResult;
import ru.practicum.mainserver.users.dto.ParticipationRequestDto;
import ru.practicum.mainserver.users.repositories.ParticipationRequestRepository;
import ru.practicum.mainserver.users.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ViewsClient viewsClient;

    @Autowired
    private ParticipationRequestRepository requestRepository;


    @Override
    public List<EventShortDto> getUserEvents(long userId, Integer from, Integer size) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%s not found.", userId));
        }

        List<Event> events = eventRepository.findAllByUserIdAndFromAndSize(userId, from, size);

        List<Long> eventIds = events.stream().map(Event::getId).toList();

        Map<Long, Long> requestsCount = requestRepository.getApprovedRequestsCount(eventIds);

        Map<Long, Integer> viewsCount = viewsClient.getViewsByList(eventIds);

        return events.stream()
                .map(event ->
                        EventMapper.toShortDto(
                                    event,
                                    requestsCount.getOrDefault(event.getId(), 0L),
                                    viewsCount.getOrDefault(event.getId(), 0)
                                )
                )
                .toList();
    }

    @Override
    public EventFullDto createEvent(long userId, NewEventDto eventDto) {
        if (eventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException(String.format("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: %s", eventDto.getEventDate()));
        }

        Optional<Category> category = categoryRepository.findById(eventDto.getCategory());

        if (category.isEmpty()) {
            throw new NotFoundException(String.format("Category with id=%s not found.", eventDto.getCategory()));
        }

        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%s not found.", userId));
        }

        return EventMapper.toFullDto(
                eventRepository.save(
                        EventMapper.toEvent(eventDto, category.get(), user.get())
                ),
                0,
                0
        );
    }

    @Override
    public EventFullDto getUserEvent(long userId, long eventId) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%s not found.", userId));
        }

        Optional<Event> event = eventRepository.findById(eventId);

        if (event.isEmpty()) {
            throw new NotFoundException(String.format("Event with id=%s not found.", eventId));
        }

        return EventMapper.toFullDto(
                event.get(),
                requestRepository.countAllByEventIdAndStatus(eventId, PendingRequestStatus.CONFIRMED),
                viewsClient.getViews(eventId)
        );
    }

    @Override
    public EventFullDto updateUserEvent(long userId, long eventId, UpdateEventUserRequest eventUserRequest) {
        Optional<Event> event = eventRepository.findById(eventId);

        if (event.isEmpty()) {
            throw new NotFoundException(String.format("Event with id=%s not found.", eventId));
        }

        if (!EventState.isCanceledOrPending(event.get().getState())) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (eventUserRequest.getEventDate() != null) {
            if (eventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException(String.format("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: %s", eventUserRequest.getEventDate()));
            }
        }

        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%s not found.", userId));
        }

        Optional<Category> category = categoryRepository.findById(eventUserRequest.getCategory());

        if (eventUserRequest.getCategory() != 0 && category.isEmpty()) {
            throw new NotFoundException(String.format("Category with id=%s not found.", eventUserRequest.getCategory()));
        }

        UpdateMapper.mergeObjects(event.get(), EventMapper.toEvent(
                eventUserRequest,
                category.orElse(event.get().getCategory()),
                user.get()
        ));

        if (eventUserRequest.getStateAction() == EventStateActionUser.CANCEL_REVIEW) {
            event.get().setState(EventState.CANCELED);
        } else if (eventUserRequest.getStateAction() == EventStateActionUser.SEND_TO_REVIEW) {
            event.get().setState(EventState.PENDING);
        }

        Event finalEvent = eventRepository.save(event.get());

        return EventMapper.toFullDto(
                finalEvent,
                requestRepository.countAllByEventIdAndStatus(finalEvent.getId(), PendingRequestStatus.CONFIRMED),
                viewsClient.getViews(finalEvent.getId())
        );
    }

    @Override
    public List<ParticipationRequestDto> getUserEventRequests(long userId, long eventId) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%s not found.", userId));
        }

        Optional<Event> event = eventRepository.findById(eventId);

        if (event.isEmpty()) {
            throw new NotFoundException(String.format("Event with id=%s not found.", eventId));
        }

        return requestRepository.findAllByEventId(eventId).stream()
                .map(UserMapper::toPRDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult updateUserEventRequests(long userId, long eventId, EventRequestStatusUpdateRequest eventRequest) {
        Optional<Event> event = eventRepository.findById(eventId);

        if (event.isEmpty()) {
            throw new NotFoundException(String.format("Event with id=%s not found.", eventId));
        }

        if (event.get().getState() != EventState.PUBLISHED) {
            throw new ForbiddenException("Event must be published.");
        }

        if (!event.get().getRequestModeration() || event.get().getParticipantLimit() == 0) {
            throw new ConflictException("Request moderation is disabled or participant limit is 0. Requests are automatically confirmed.");
        }

        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%s not found.", userId));
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(eventRequest.getRequestIds());

        for (ParticipationRequest req : requests) {
            if (req.getEvent().getId() != eventId) {
                throw new BadRequestException("Request does not belong to this event.");
            }
            if (req.getStatus() != PendingRequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING.");
            }
        }

        int freeRequests;

        if (event.get().getParticipantLimit() == 0) {
             freeRequests = requests.size();
        } else {
            freeRequests = event.get().getParticipantLimit() - requestRepository.countAllByEventIdAndStatus(eventId, PendingRequestStatus.CONFIRMED);

            if (freeRequests <= 0) {
                throw new ConflictException("The participant limit has been reached.");
            }
        }


        List<ParticipationRequest> cancelledRequests = new ArrayList<>(requests);

        List<ParticipationRequest> confirmedRequests = new ArrayList<>();

        if (eventRequest.getStatus() == UpdateEventRequestStatus.CONFIRMED) {
            for (int i = 0; i < Math.min(freeRequests, requests.size()); i++) {
                confirmedRequests.add(requests.get(i));

                cancelledRequests.remove(i);
            }
        }

        return new EventRequestStatusUpdateResult(
                requestRepository.saveAll(confirmedRequests.stream()
                        .peek(request -> request.setStatus(PendingRequestStatus.CONFIRMED))
                        .toList()).stream()
                            .map(UserMapper::toPRDto)
                            .toList(),
                requestRepository.saveAll(cancelledRequests.stream()
                        .peek(request -> request.setStatus(PendingRequestStatus.REJECTED))
                        .toList()).stream()
                            .map(UserMapper::toPRDto)
                            .toList()
        );
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(long userId) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%s not found.", userId));
        }

        return requestRepository.findAllByUserId(userId).stream()
                .map(UserMapper::toPRDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto createUserEventRequest(long userId, long eventId) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%s not found.", userId));
        }

        Optional<Event> event = eventRepository.findById(eventId);

        if (event.isEmpty()) {
            throw new NotFoundException(String.format("Event with id=%s not found.", eventId));
        }

        if (requestRepository.findAllByUserIdAndEventId(userId, eventId).isPresent()) {
            throw new ConflictException("Request for participation has already been sent earlier.");
        }

        if (event.get().getInitiator().getId() == userId) {
            throw new ConflictException("You cannot submit a request for your event.");
        }

        if (event.get().getState() != EventState.PUBLISHED) {
            throw new ConflictException("You cannot participate in an unpublished event.");
        }

        if (event.get().getParticipantLimit() != 0 && event.get().getParticipantLimit() - requestRepository.countAllByEventIdAndStatus(eventId, PendingRequestStatus.CONFIRMED) <= 0) {
            throw new ConflictException("The participant limit has been reached.");
        }

        ParticipationRequest request = new ParticipationRequest(
                0L,
                event.get(),
                user.get(),
                PendingRequestStatus.PENDING,
                LocalDateTime.now()
        );

        if (!event.get().getRequestModeration() || event.get().getParticipantLimit() == 0) {
            request.setStatus(PendingRequestStatus.CONFIRMED);
        }

        return UserMapper.toPRDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancelUserRequest(long userId, long requestId) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%s not found.", userId));
        }

        Optional<ParticipationRequest> request = requestRepository.findById(requestId);

        if (request.isEmpty()) {
            throw new NotFoundException(String.format("ParticipationRequest with id=%s not found.", requestId));
        }

        request.get().setStatus(PendingRequestStatus.CANCELED);

        return UserMapper.toPRDto(requestRepository.save(request.get()));
    }
}
