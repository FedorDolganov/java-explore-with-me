package ru.practicum.mainserver.events.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.mainserver.events.Event;
import ru.practicum.mainserver.events.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("select e " +
            "from Event e " +
            "where e.initiator.id = ?1 " +
            "order by e.id " +
            "limit ?3 " +
            "offset ?2")
    List<Event> findAllByUserIdAndFromAndSize(long userId, int from, int size);

    @Query("select e " +
            "from Event e " +
            "where e.state =  ?1 " +
            "and (?2 is null or lower(e.annotation) like lower(concat('%', cast(?2 as string), '%')) or lower(e.description) like lower(concat('%', cast(?2 as string), '%')) or lower(e.title) like lower(concat('%', cast(?2 as string), '%'))) " +
            "and (?3 is null or e.category.id in ?3) " +
            "and (?4 is null or e.paid = ?4) " +
            "and e.eventDate >= ?5 " +
            "and e.eventDate <= ?6 " +
            "order by e.id " +
            "limit ?8 " +
            "offset ?7")
    List<Event> findAllByFilters(EventState state, String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    @Query("select e " +
            "from Event e " +
            "where (?1 is null or e.initiator.id in ?1) " +
            "and (?2 is null or e.state in ?2)" +
            "and (?3 is null or e.category.id in ?3) " +
            "and (cast(?4 as localdatetime) is null or e.eventDate >= ?4) " +
            "and (cast(?5 as localdatetime) is null or e.eventDate <= ?5) " +
            "order by e.id " +
            "limit ?7 " +
            "offset ?6")
    List<Event> findAllByFiltersAdmin(List<Long> users, List<EventState> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    @Query("select e " +
            "from Event e " +
            "where e.id in ?1")
    List<Event> findByIds(List<Long> events);

    @Query("select e " +
            "from Event e " +
            "where e.category.id = ?1")
    List<Event> findAllByCatId(long catId);

}
