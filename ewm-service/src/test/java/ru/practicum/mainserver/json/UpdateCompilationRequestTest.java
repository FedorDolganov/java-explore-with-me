package ru.practicum.mainserver.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.mainserver.compilations.dto.UpdateCompilationRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UpdateCompilationRequestTest {

    @Autowired
    private JacksonTester<UpdateCompilationRequest> updateCompilationRequestTester;

    @Test
    void testUpdateCompilationRequestSerialization() throws Exception {
        UpdateCompilationRequest dto = new UpdateCompilationRequest();
        dto.setEvents(List.of(1L, 2L));
        dto.setPinned(false);
        dto.setTitle("Updated compilation");

        JsonContent<UpdateCompilationRequest> json = updateCompilationRequestTester.write(dto);

        assertThat(json).isNotNull();
        assertThat(json).hasJsonPathArrayValue("$.events");
        assertThat(json).hasJsonPathBooleanValue("$.pinned");
        assertThat(json).hasJsonPathStringValue("$.title");
        assertThat(json).extractingJsonPathArrayValue("$.events").hasSize(2);
        assertThat(json).extractingJsonPathBooleanValue("$.pinned").isFalse();
        assertThat(json).extractingJsonPathStringValue("$.title").isEqualTo("Updated compilation");
    }

}
