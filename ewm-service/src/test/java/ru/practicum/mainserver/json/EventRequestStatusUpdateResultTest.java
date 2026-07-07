package ru.practicum.mainserver.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.mainserver.users.PendingRequestStatus;
import ru.practicum.mainserver.users.dto.EventRequestStatusUpdateResult;
import ru.practicum.mainserver.users.dto.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class EventRequestStatusUpdateResultTest {

    @Autowired
    private JacksonTester<EventRequestStatusUpdateResult> eventRequestStatusUpdateResultTester;

    @Test
    void testEventRequestStatusUpdateResultSerialization() throws Exception {
        EventRequestStatusUpdateResult dto = new EventRequestStatusUpdateResult();

        ParticipationRequestDto confirmed = new ParticipationRequestDto();
        confirmed.setId(1L);
        confirmed.setEvent(10L);
        confirmed.setRequester(5L);
        confirmed.setStatus(PendingRequestStatus.CONFIRMED);
        confirmed.setCreated(LocalDateTime.of(2020, 1, 1, 10, 0));

        ParticipationRequestDto rejected = new ParticipationRequestDto();
        rejected.setId(2L);
        rejected.setEvent(10L);
        rejected.setRequester(6L);
        rejected.setStatus(PendingRequestStatus.REJECTED);
        rejected.setCreated(LocalDateTime.of(2020, 1, 1, 10, 0));

        dto.setConfirmedRequests(List.of(confirmed));
        dto.setRejectedRequests(List.of(rejected));

        JsonContent<EventRequestStatusUpdateResult> json = eventRequestStatusUpdateResultTester.write(dto);

        assertThat(json).isNotNull();
        assertThat(json).hasJsonPathArrayValue("$.confirmedRequests");
        assertThat(json).hasJsonPathArrayValue("$.rejectedRequests");
        assertThat(json).extractingJsonPathArrayValue("$.confirmedRequests").hasSize(1);
        assertThat(json).extractingJsonPathArrayValue("$.rejectedRequests").hasSize(1);
        assertThat(json).extractingJsonPathStringValue("$.confirmedRequests[0].status").isEqualTo("CONFIRMED");
        assertThat(json).extractingJsonPathStringValue("$.rejectedRequests[0].status").isEqualTo("REJECTED");
    }

}
