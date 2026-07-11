package ru.practicum.mainserver.users.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.mainserver.users.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c " +
            "from Comment c " +
            "where c.event.id in ?1 " +
            "group by c.event.id")
    List<Comment> findCommensByEventsIds(List<Long> eventIds);

    @Query("select c " +
            "from Comment c " +
            "where c.event.id = ?1")
    List<Comment> findCommensByEventId(long eventId);

    @Query("select count(c) > 0 " +
            "from Comment c " +
            "where c.author.id = ?1 " +
            "and c.event.id = ?2")
    boolean userHasCommentsToThisEvent(long userId, long eventId);

    @Query("select c " +
            "from Comment c " +
            "where c.id in ?1 or ?1 is null " +
            "order by c.id " +
            "limit ?3 " +
            "offset ?2")
    List<Comment> findCommensByEventsIdsAndSizeAndFrom(String[] ids, Integer from, Integer size);
}
