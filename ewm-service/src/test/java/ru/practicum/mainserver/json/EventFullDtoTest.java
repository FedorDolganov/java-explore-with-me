package ru.practicum.mainserver.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.mainserver.categories.dto.CategoryDto;
import ru.practicum.mainserver.events.EventState;
import ru.practicum.mainserver.events.dto.EventFullDto;
import ru.practicum.mainserver.events.dto.Location;
import ru.practicum.mainserver.users.dto.UserShortDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class EventFullDtoTest {

    @Autowired
    private JacksonTester<EventFullDto> eventFullDtoTester;

    @Test
    void testEventFullDtoSerialization() throws Exception {
        EventFullDto dto = new EventFullDto();
        dto.setId(1L);
        dto.setAnnotation("Test annotation");
        dto.setConfirmedRequests(1);
        dto.setCreatedOn(LocalDateTime.of(2020, 1, 1, 10, 0));
        dto.setDescription("Test desc");
        dto.setEventDate(LocalDateTime.of(2020, 1, 1, 10, 0));
        dto.setPaid(true);
        dto.setParticipantLimit(1);
        dto.setPublishedOn(LocalDateTime.of(2020, 1, 1, 10, 0));
        dto.setRequestModeration(true);
        dto.setState(EventState.PUBLISHED);
        dto.setTitle("Test title");
        dto.setViews(1);

        CategoryDto category = new CategoryDto();
        category.setId(1L);
        category.setName("Test category");
        dto.setCategory(category);

        UserShortDto initiator = new UserShortDto();
        initiator.setId(3L);
        initiator.setName("Test user");
        dto.setInitiator(initiator);

        Location location = new Location();
        location.setLat(55.754167f);
        location.setLon(37.62f);
        dto.setLocation(location);

        JsonContent<EventFullDto> json = eventFullDtoTester.write(dto);

        assertThat(json).isNotNull();
        assertThat(json).hasJsonPathNumberValue("$.id");
        assertThat(json).hasJsonPathStringValue("$.annotation");
        assertThat(json).hasJsonPathNumberValue("$.confirmedRequests");
        assertThat(json).hasJsonPathStringValue("$.createdOn");
        assertThat(json).hasJsonPathStringValue("$.description");
        assertThat(json).hasJsonPathStringValue("$.eventDate");
        assertThat(json).hasJsonPathBooleanValue("$.paid");
        assertThat(json).hasJsonPathNumberValue("$.participantLimit");
        assertThat(json).hasJsonPathStringValue("$.publishedOn");
        assertThat(json).hasJsonPathBooleanValue("$.requestModeration");
        assertThat(json).hasJsonPathStringValue("$.state");
        assertThat(json).hasJsonPathStringValue("$.title");
        assertThat(json).hasJsonPathNumberValue("$.views");
        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json).extractingJsonPathStringValue("$.title").isEqualTo("Test title");
        assertThat(json).extractingJsonPathStringValue("$.state").isEqualTo("PUBLISHED");
    }

}
