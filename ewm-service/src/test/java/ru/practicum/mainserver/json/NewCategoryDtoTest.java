package ru.practicum.mainserver.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.mainserver.categories.dto.NewCategoryDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class NewCategoryDtoTest {

    @Autowired
    private JacksonTester<NewCategoryDto> newCategoryDtoTester;

    @Test
    void testNewCategoryDtoSerialization() throws Exception {
        NewCategoryDto dto = new NewCategoryDto(
                "Test category"
        );

        JsonContent<NewCategoryDto> json = newCategoryDtoTester.write(dto);

        assertThat(json).isNotNull();
        assertThat(json).hasJsonPathStringValue("$.name");
        assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo("Test category");
    }

}
