package ru.practicum.mainserver.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.mainserver.compilations.dto.NewCompilationDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class NewCompilationTest {

    @Autowired
    private JacksonTester<NewCompilationDto> newCompilationDtoTester;


    @Test
    void testNewCompilationDtoSerialization() throws Exception {
        NewCompilationDto dto = new NewCompilationDto();
        dto.setEvents(List.of(1L, 2L, 3L));
        dto.setPinned(true);
        dto.setTitle("Test compilation");

        JsonContent<NewCompilationDto> json = newCompilationDtoTester.write(dto);

        assertThat(json).isNotNull();
        assertThat(json).hasJsonPathArrayValue("$.events");
        assertThat(json).hasJsonPathBooleanValue("$.pinned");
        assertThat(json).hasJsonPathStringValue("$.title");
        assertThat(json).extractingJsonPathArrayValue("$.events").hasSize(3);
        assertThat(json).extractingJsonPathBooleanValue("$.pinned").isTrue();
        assertThat(json).extractingJsonPathStringValue("$.title").isEqualTo("Test compilation");
    }

}
