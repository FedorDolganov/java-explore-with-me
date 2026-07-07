package ru.practicum.mainserver.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.mainserver.users.dto.NewUserRequest;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class NewUserRequestTest {

    @Autowired
    private JacksonTester<NewUserRequest> newUserRequestTester;


    @Test
    void testNewUserRequestSerialization() throws Exception {
        NewUserRequest dto = new NewUserRequest();
        dto.setEmail("testemail@yandex.ru");
        dto.setName("Test user");

        JsonContent<NewUserRequest> json = newUserRequestTester.write(dto);

        assertThat(json).isNotNull();
        assertThat(json).hasJsonPathStringValue("$.email");
        assertThat(json).hasJsonPathStringValue("$.name");
        assertThat(json).extractingJsonPathStringValue("$.email").isEqualTo("testemail@yandex.ru");
        assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo("Test user");
    }

}
