package ru.practicum.mainserver.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.mainserver.users.PendingRequestStatus;
import ru.practicum.mainserver.users.dto.ParticipationRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ParticipationRequestDtoTest {

    @Autowired
    private JacksonTester<ParticipationRequestDto> participationRequestDtoTester;

    @Test
    void testParticipationRequestDtoSerialization() throws Exception {
        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setCreated(LocalDateTime.of(2020, 1, 1, 10, 0));
        dto.setEvent(1L);
        dto.setId(3L);
        dto.setRequester(2L);
        dto.setStatus(PendingRequestStatus.PENDING);

        JsonContent<ParticipationRequestDto> json = participationRequestDtoTester.write(dto);

        assertThat(json).isNotNull();
        assertThat(json).hasJsonPathStringValue("$.created");
        assertThat(json).hasJsonPathNumberValue("$.event");
        assertThat(json).hasJsonPathNumberValue("$.id");
        assertThat(json).hasJsonPathNumberValue("$.requester");
        assertThat(json).hasJsonPathStringValue("$.status");
        assertThat(json).extractingJsonPathNumberValue("$.event").isEqualTo(1);
        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(3);
        assertThat(json).extractingJsonPathNumberValue("$.requester").isEqualTo(2);
        assertThat(json).extractingJsonPathStringValue("$.status").isEqualTo("PENDING");
    }


}
