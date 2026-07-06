package ru.practicum.mainserver.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.mainserver.events.dto.Location;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class LocationTest {

    @Autowired
    private JacksonTester<Location> locationTester;


    @Test
    void testLocationSerialization() throws Exception {
        Location dto = new Location(
                52,
                38
        );

        JsonContent<Location> json = locationTester.write(dto);

        assertThat(json).isNotNull();
        assertThat(json).hasJsonPathNumberValue("$.lat");
        assertThat(json).hasJsonPathNumberValue("$.lon");
        assertThat(json).extractingJsonPathNumberValue("$.lat").isEqualTo(52.0);
        assertThat(json).extractingJsonPathNumberValue("$.lon").isEqualTo(38.0);
    }

}
