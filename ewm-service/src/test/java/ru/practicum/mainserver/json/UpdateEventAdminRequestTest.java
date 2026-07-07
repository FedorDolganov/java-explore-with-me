package ru.practicum.mainserver.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.mainserver.events.EventStateAction;
import ru.practicum.mainserver.events.dto.Location;
import ru.practicum.mainserver.events.dto.UpdateEventAdminRequest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UpdateEventAdminRequestTest {

    @Autowired
    private JacksonTester<UpdateEventAdminRequest> updateEventAdminRequestTester;

    @Test
    void testUpdateEventAdminRequestSerialization() throws Exception {
        UpdateEventAdminRequest dto = new UpdateEventAdminRequest();
        dto.setAnnotation("Test annotation");
        dto.setCategory(3L);
        dto.setDescription("Test desc");
        dto.setEventDate(LocalDateTime.of(2020, 1, 1, 10, 0));
        dto.setPaid(true);
        dto.setParticipantLimit(7);
        dto.setRequestModeration(false);
        dto.setStateAction(EventStateAction.PUBLISH_EVENT);
        dto.setTitle("Test title");

        Location location = new Location();
        location.setLat(55.754167f);
        location.setLon(37.62f);
        dto.setLocation(location);

        JsonContent<UpdateEventAdminRequest> json = updateEventAdminRequestTester.write(dto);

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
        assertThat(json).extractingJsonPathStringValue("$.stateAction").isEqualTo("PUBLISH_EVENT");
    }

}
