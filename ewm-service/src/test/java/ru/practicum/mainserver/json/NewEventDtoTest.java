package ru.practicum.mainserver.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.mainserver.events.dto.Location;
import ru.practicum.mainserver.events.dto.NewEventDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@JsonTest
public class NewEventDtoTest {

    @Autowired
    private JacksonTester<NewEventDto> newEventDtoTester;

    @Test
    void testNewEventDtoSerialization() throws Exception {
        NewEventDto dto = new NewEventDto();
        dto.setAnnotation("Test annotation");
        dto.setCategory(2L);
        dto.setDescription("Test desc");
        dto.setEventDate(LocalDateTime.of(2020, 1, 1, 10, 0));
        dto.setPaid(true);
        dto.setParticipantLimit(10);
        dto.setRequestModeration(true);
        dto.setTitle("Test title");

        Location location = new Location();
        location.setLat(55.754167f);
        location.setLon(37.62f);
        dto.setLocation(location);

        JsonContent<NewEventDto> json = newEventDtoTester.write(dto);

        assertThat(json).isNotNull();
        assertThat(json).hasJsonPathStringValue("$.annotation");
        assertThat(json).hasJsonPathNumberValue("$.category");
        assertThat(json).hasJsonPathStringValue("$.description");
        assertThat(json).hasJsonPathStringValue("$.eventDate");
        assertThat(json).hasJsonPathBooleanValue("$.paid");
        assertThat(json).hasJsonPathNumberValue("$.participantLimit");
        assertThat(json).hasJsonPathBooleanValue("$.requestModeration");
        assertThat(json).hasJsonPathStringValue("$.title");
        assertThat(json).extractingJsonPathStringValue("$.annotation").isEqualTo("Test annotation");
        assertThat(json).extractingJsonPathStringValue("$.title").isEqualTo("Test title");
    }

}
