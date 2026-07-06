package ru.practicum.mainserver.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.mainserver.categories.dto.CategoryDto;
import ru.practicum.mainserver.events.dto.EventShortDto;
import ru.practicum.mainserver.users.dto.UserShortDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class EventShortDtoTest {

    @Autowired
    private JacksonTester<EventShortDto> eventShortDtoTester;

    @Test
    void testEventShortDtoSerialization() throws Exception {
        EventShortDto dto = new EventShortDto();
        dto.setId(1L);
        dto.setAnnotation("Test annotation");
        dto.setConfirmedRequests(1);
        dto.setEventDate(LocalDateTime.of(2020, 1, 1, 10, 0));
        dto.setPaid(true);
        dto.setTitle("Test title");
        dto.setViews(1);

        CategoryDto category = new CategoryDto();
        category.setId(1L);
        category.setName("Test category");
        dto.setCategory(category);

        UserShortDto initiator = new UserShortDto();
        initiator.setId(1L);
        initiator.setName("Test user");
        dto.setInitiator(initiator);

        JsonContent<EventShortDto> json = eventShortDtoTester.write(dto);

        assertThat(json).isNotNull();
        assertThat(json).hasJsonPathNumberValue("$.id");
        assertThat(json).hasJsonPathStringValue("$.annotation");
        assertThat(json).hasJsonPathNumberValue("$.confirmedRequests");
        assertThat(json).hasJsonPathStringValue("$.eventDate");
        assertThat(json).hasJsonPathBooleanValue("$.paid");
        assertThat(json).hasJsonPathStringValue("$.title");
        assertThat(json).hasJsonPathNumberValue("$.views");
        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json).extractingJsonPathStringValue("$.title").isEqualTo("Test title");
    }

}
