package ru.practicum.mainserver.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.mainserver.categories.dto.CategoryDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CategoryDtoTest {

    @Autowired
    private JacksonTester<CategoryDto> categoryDtoTester;

    @Test
    void testCategoryDtoSerialization() throws Exception {
        CategoryDto dto = new CategoryDto(
                1L,
                "Test category"
        );

        JsonContent<CategoryDto> json = categoryDtoTester.write(dto);

        assertThat(json).isNotNull();
        assertThat(json).hasJsonPathNumberValue("$.id");
        assertThat(json).hasJsonPathStringValue("$.name");
        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo("Test category");
    }

}
