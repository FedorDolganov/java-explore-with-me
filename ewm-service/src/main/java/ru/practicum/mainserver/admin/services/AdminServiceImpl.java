package ru.practicum.mainserver.admin.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainserver.categories.Category;
import ru.practicum.mainserver.categories.dto.CategoryDto;
import ru.practicum.mainserver.categories.dto.NewCategoryDto;
import ru.practicum.mainserver.categories.repositories.CategoryRepository;
import ru.practicum.mainserver.client.ViewsClient;
import ru.practicum.mainserver.users.Comment;
import ru.practicum.mainserver.users.dto.CommentDto;
import ru.practicum.mainserver.users.dto.UpdateCommentDto;
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
import ru.practicum.mainserver.exceptions.BadRequestException;
import ru.practicum.mainserver.exceptions.ConflictException;
import ru.practicum.mainserver.exceptions.NotFoundException;
import ru.practicum.mainserver.mappers.*;
import ru.practicum.mainserver.users.PendingRequestStatus;
import ru.practicum.mainserver.users.dto.NewUserRequest;
import ru.practicum.mainserver.users.dto.UserDto;
import ru.practicum.mainserver.users.repositories.CommentRepository;
import ru.practicum.mainserver.users.repositories.ParticipationRequestRepository;
import ru.practicum.mainserver.users.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompilationRepository compilationRepository;

    @Autowired
    private ViewsClient viewsClient;

    @Autowired
    private ParticipationRequestRepository requestRepository;

    @Autowired
    private CommentRepository commentRepository;



    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto categoryDto) {
        return CategoryMapper.toDto(categoryRepository.save(CategoryMapper.to(categoryDto)));
    }

    @Override
    @Transactional
    public void deleteCategory(long catId) {
        if (!eventRepository.findAllByCatId(catId).isEmpty()) {
            throw new ConflictException("Category already has linked events");
        }

        categoryRepository.deleteById(catId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(long catId, NewCategoryDto categoryDto) {
        Optional<Category> category = categoryRepository.findById(catId);

        if (category.isEmpty()) {
            throw new NotFoundException(String.format("Category with id=%s was not found", catId));
        }

        category.get().setName(categoryDto.getName());

        return CategoryMapper.toDto(categoryRepository.save(category.get()));
    }

    @Override
    public List<EventFullDto> getEvents(List<Long> users, List<EventState> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        List<Event> events = eventRepository.findAllByFiltersAdmin(users, states, categories, rangeStart, rangeEnd, from, size);

        List<Long> ids = events.stream().map(Event::getId).toList();

        Map<Long, Integer> viewsCount = viewsClient.getViewsByList(ids);
        Map<Long, Long> requestsCount = requestRepository.getApprovedRequestsCount(ids);
        List<Comment> comments = commentRepository.findCommensByEventsIds(ids);

        return events.stream()
                .map(event ->  EventMapper.toFullDto(
                        event,
                        requestsCount.getOrDefault(event.getId(), 0L),
                        viewsCount.getOrDefault(event.getId(), 0),
                        comments.stream()
                                .filter(comment -> comment.getEvent().getId() == event.getId())
                                .map(CommentMapper::toDto)
                                .toList()
                ))
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(long eventId, UpdateEventAdminRequest eventAdminRequest) {
        Optional<Event> event = eventRepository.findById(eventId);

        if (event.isEmpty()) {
            throw new NotFoundException(String.format("Event with id=%s was not found.", event));
        }

        Optional<Category> category = categoryRepository.findById(eventAdminRequest.getCategory());

        if (eventAdminRequest.getCategory() != 0 && category.isEmpty()) {
            throw new NotFoundException(String.format("Event with id=%s was not found.", event));
        }

        if (eventAdminRequest.getEventDate() != null) {
            if (eventAdminRequest.getEventDate().isBefore(event.get().getCreatedOn().plusHours(1))) {
                throw new BadRequestException("The start date of the event being modified must be no earlier than one hour from the publication date.");
            }
        }

        UpdateMapper.mergeObjects(event.get(), EventMapper.toEvent(eventAdminRequest, category.orElse(null)));

        if (eventAdminRequest.getStateAction() == EventStateAction.PUBLISH_EVENT) {
            if (event.get().getState() != EventState.PENDING) {
                throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.get().getState());
            }

            event.get().setState(EventState.PUBLISHED);
        } else if (eventAdminRequest.getStateAction() == EventStateAction.REJECT_EVENT) {
            if (event.get().getState() == EventState.PUBLISHED) {
                throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.get().getState());
            }

            event.get().setState(EventState.CANCELED);
        }

        return EventMapper.toFullDto(
                eventRepository.save(event.get()),
                requestRepository.countAllByEventIdAndStatus(eventId, PendingRequestStatus.CONFIRMED),
                viewsClient.getViews(eventId),
                commentRepository.findCommensByEventId(eventId).stream()
                        .map(CommentMapper::toDto)
                        .toList()
        );
    }

    @Override
    public List<UserDto> getUsers(String[] ids, Integer from, Integer size) {
        return userRepository.findAllByIdsAndFromAndSize(ids, from, size).stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest userRequest) {
        return UserMapper.toDto(userRepository.save(UserMapper.to(userRequest)));
    }

    @Override
    @Transactional
    public void deleteUser(long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto compilationDto) {
        return CompilationMapper.toDto(
                compilationRepository.save(
                        CompilationMapper.to(compilationDto, eventRepository.findByIds(compilationDto.getEvents()))
                ),
                requestRepository.getApprovedRequestsCount(compilationDto.getEvents()),
                viewsClient.getViewsByList(compilationDto.getEvents())
        );
    }

    @Override
    @Transactional
    public void deteleCompilation(long compId) {
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(long compId, UpdateCompilationRequest compilationRequest) {
        Optional<Compilation> compilation = compilationRepository.findById(compId);

        if (compilation.isEmpty()) {
            throw new NotFoundException(String.format("Compilation with id=%s was not found", compId));
        }

        compilation.get().getEvents().addAll(eventRepository.findAllById(compilationRequest.getEvents()));

        UpdateMapper.mergeObjects(compilation.get(), CompilationMapper.to(compilationRequest, compId, compilation.get().getEvents()));

        List<Long> eventIds = compilation.get().getEvents().stream().map(Event::getId).toList();

        return CompilationMapper.toDto(
                compilationRepository.save(compilation.get()),
                requestRepository.getApprovedRequestsCount(eventIds),
                viewsClient.getViewsByList(eventIds)
        );
    }

    @Override
    @Transactional
    public CommentDto updateComments(long comId, UpdateCommentDto commentDto) {
        Optional<Comment> comment = commentRepository.findById(comId);

        if (comment.isEmpty()) {
            throw new NotFoundException(String.format("Comment id=%s not found.", comId));
        }

        comment.get().setText(commentDto.getText());

        return CommentMapper.toDto(
                commentRepository.save(comment.get())
        );
    }

    @Override
    public List<CommentDto> getComments(String[] ids, Integer from, Integer size) {
        return commentRepository.findCommensByEventsIdsAndSizeAndFrom(ids, from, size).stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteComments(long comId) {
        if (!commentRepository.existsById(comId)) {
            throw new NotFoundException(String.format("Comment id=%s not found.", comId));
        }

        commentRepository.deleteById(comId);
    }
}
