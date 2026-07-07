package ru.practicum.mainserver.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.mainserver.users.UpdateEventRequestStatus;
import ru.practicum.mainserver.users.dto.EventRequestStatusUpdateRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class EventRequestStatusUpdateRequestTest {

    @Autowired
    private JacksonTester<EventRequestStatusUpdateRequest> eventRequestStatusUpdateRequestTester;

    @Test
    void testEventRequestStatusUpdateRequestSerialization() throws Exception {
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest();
        dto.setRequestIds(List.of(1L, 2L, 3L));
        dto.setStatus(UpdateEventRequestStatus.CONFIRMED);

        JsonContent<EventRequestStatusUpdateRequest> json = eventRequestStatusUpdateRequestTester.write(dto);

        assertThat(json).isNotNull();
        assertThat(json).hasJsonPathArrayValue("$.requestIds");
        assertThat(json).hasJsonPathStringValue("$.status");
        assertThat(json).extractingJsonPathArrayValue("$.requestIds").hasSize(3);
        assertThat(json).extractingJsonPathStringValue("$.status").isEqualTo("CONFIRMED");
    }

}
