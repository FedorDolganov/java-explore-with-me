package ru.practicum.mainserver.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.mainserver.users.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserDtoTest {

    @Autowired
    private JacksonTester<UserDto> userDtoTester;

    @Test
    void testUserDtoSerialization() throws Exception {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setEmail("testemail@yandex.ru");
        dto.setName("Test user");

        JsonContent<UserDto> json = userDtoTester.write(dto);

        assertThat(json).isNotNull();
        assertThat(json).hasJsonPathNumberValue("$.id");
        assertThat(json).hasJsonPathStringValue("$.email");
        assertThat(json).hasJsonPathStringValue("$.name");
        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json).extractingJsonPathStringValue("$.email").isEqualTo("testemail@yandex.ru");
        assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo("Test user");
    }

}
