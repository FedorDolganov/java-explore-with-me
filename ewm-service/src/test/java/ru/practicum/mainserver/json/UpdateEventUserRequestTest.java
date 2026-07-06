package ru.practicum.mainserver.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.mainserver.events.EventStateActionUser;
import ru.practicum.mainserver.events.dto.Location;
import ru.practicum.mainserver.events.dto.UpdateEventUserRequest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@JsonTest
public class UpdateEventUserRequestTest {

    @Autowired
    private JacksonTester<UpdateEventUserRequest> updateEventUserRequestTester;


    @Test
    void testUpdateEventUserRequestSerialization() throws Exception {
        UpdateEventUserRequest dto = new UpdateEventUserRequest();
        dto.setAnnotation("Test annotation");
        dto.setCategory(3L);
        dto.setDescription("Test desc");
        dto.setEventDate(LocalDateTime.of(2020, 1, 1, 10, 0));
        dto.setPaid(true);
        dto.setParticipantLimit(7);
        dto.setRequestModeration(false);
        dto.setStateAction(EventStateActionUser.SEND_TO_REVIEW);
        dto.setTitle("Test title");
        dto.setLocation(
                new Location(
                        52,
                        38
                )
        );

        JsonContent<UpdateEventUserRequest> json = updateEventUserRequestTester.write(dto);

        assertThat(json).isNotNull();
        assertThat(json).hasJsonPathStringValue("$.annotation");
        assertThat(json).hasJsonPathNumberValue("$.category");
        assertThat(json).hasJsonPathStringValue("$.description");
        assertThat(json).hasJsonPathStringValue("$.eventDate");
        assertThat(json).hasJsonPathBooleanValue("$.paid");
        assertThat(json).hasJsonPathNumberValue("$.participantLimit");
        assertThat(json).hasJsonPathBooleanValue("$.requestModeration");
        assertThat(json).hasJsonPathStringValue("$.stateAction");
        assertThat(json).hasJsonPathStringValue("$.title");
        assertThat(json).extractingJsonPathStringValue("$.stateAction").isEqualTo("SEND_TO_REVIEW");
    }

}
