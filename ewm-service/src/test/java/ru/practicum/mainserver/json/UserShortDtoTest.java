package ru.practicum.mainserver.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.mainserver.users.dto.UserShortDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserShortDtoTest {

    @Autowired
    private JacksonTester<UserShortDto> userShortDtoTester;


    @Test
    void testUserShortDtoSerialization() throws Exception {
        UserShortDto dto = new UserShortDto();
        dto.setId(1L);
        dto.setName("Test user");

        JsonContent<UserShortDto> json = userShortDtoTester.write(dto);

        assertThat(json).isNotNull();
        assertThat(json).hasJsonPathNumberValue("$.id");
        assertThat(json).hasJsonPathStringValue("$.name");
        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo("Test user");
    }

}
