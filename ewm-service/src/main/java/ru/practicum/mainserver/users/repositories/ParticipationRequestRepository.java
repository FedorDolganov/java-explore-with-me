package ru.practicum.mainserver.users.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.mainserver.users.ParticipationRequest;
import ru.practicum.mainserver.users.PendingRequestStatus;
import ru.practicum.mainserver.users.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    @Query("select pr.event.id, count(pr) " +
            "from ParticipationRequest pr " +
            "where pr.event.id in ?1 " +
            "and pr.status = ?2 " +
            "group by pr.event.id")
    List<Object[]> findApprovedByEventIds(List<Long> eventIds, PendingRequestStatus status);

    @Query("select pr " +
            "from ParticipationRequest pr " +
            "where pr.event.id = ?1")
    List<ParticipationRequest> findAllByEventId(long eventId);

    @Query("select count(pr) " +
            "from ParticipationRequest pr " +
            "where pr.event.id = ?1 " +
            "and pr.status = ?2")
    int countAllByEventIdAndStatus(long eventId, PendingRequestStatus status);

    @Query("select pr " +
            "from ParticipationRequest pr " +
            "where pr.requester.id = ?1")
    List<ParticipationRequest> findAllByUserId(long userId);

    @Query("select pr " +
            "from ParticipationRequest pr " +
            "where pr.requester.id = ?1 " +
            "and pr.event.id = ?2")
    Optional<ParticipationRequest> findAllByUserIdAndEventId(long userId, long eventId);


    default Map<Long, Integer> getApprovedRequestsCount(List<Long> eventIds) {
        return findApprovedByEventIds(eventIds, PendingRequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Integer) row[1],
                        (existing, replacement) -> existing
                ));
    }

}
