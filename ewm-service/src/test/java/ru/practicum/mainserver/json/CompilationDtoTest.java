package ru.practicum.mainserver.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.mainserver.categories.dto.CategoryDto;
import ru.practicum.mainserver.compilations.dto.CompilationDto;
import ru.practicum.mainserver.events.dto.EventShortDto;
import ru.practicum.mainserver.users.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CompilationDtoTest {

    @Autowired
    private JacksonTester<CompilationDto> compilationDtoTester;

    @Test
    void testCompilationDtoSerialization() throws Exception {
        CompilationDto dto = new CompilationDto();
        dto.setId(1L);
        dto.setPinned(true);
        dto.setTitle("Test compilation");

        UserShortDto userShort = new UserShortDto();
        userShort.setId(3L);
        userShort.setName("Test user");

        CategoryDto category = new CategoryDto();
        category.setId(1L);
        category.setName("Test name");

        EventShortDto eventShort = new EventShortDto();
        eventShort.setId(1L);
        eventShort.setAnnotation("Test Annotation");
        eventShort.setCategory(category);
        eventShort.setConfirmedRequests(1);
        eventShort.setEventDate(LocalDateTime.of(2020, 1, 1, 10, 0));
        eventShort.setInitiator(userShort);
        eventShort.setPaid(true);
        eventShort.setTitle("Test title");
        eventShort.setViews(1);

        dto.setEvents(List.of(eventShort));

        JsonContent<CompilationDto> json = compilationDtoTester.write(dto);

        assertThat(json).isNotNull();
        assertThat(json).hasJsonPathNumberValue("$.id");
        assertThat(json).hasJsonPathBooleanValue("$.pinned");
        assertThat(json).hasJsonPathStringValue("$.title");
        assertThat(json).hasJsonPathArrayValue("$.events");
        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json).extractingJsonPathBooleanValue("$.pinned").isTrue();
        assertThat(json).extractingJsonPathStringValue("$.title").isEqualTo("Test compilation");
        assertThat(json).extractingJsonPathArrayValue("$.events").hasSize(1);
    }


}
