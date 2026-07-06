package ru.practicum.mainserver.client;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.mainserver.client.dto.HitDto;
import ru.practicum.mainserver.client.dto.StatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ViewsClient extends BaseClient {

    private final ObjectMapper objectMapper;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Autowired
    public ViewsClient(@Value("${rwm-stats-server.url}") String serverUrl, RestTemplateBuilder builder, ObjectMapper objectMapper) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );

        this.objectMapper = objectMapper;
    }


    public Map<Long, Integer> getViewsByList(List<Long> ids) {
        String uris = "";

        for (long uri : ids) {
            uris = uri + "&uris=" + uri;
        }

        ResponseEntity<Object> response = get(String.format("/stats?start=%s&unique=true&end=%s" + uris, formatter.format(LocalDateTime.MIN), formatter.format(LocalDateTime.now())));

        Map<Long, Integer> views = new HashMap<>();

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JavaType type = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, StatsDto.class);

            List<StatsDto> body = objectMapper.convertValue(response.getBody(), type);

            for (StatsDto stat : body) {
                views.put(Long.parseLong(stat.getUri().replaceFirst("/events/", "")), stat.getHits());
            }
        }

        return views;
    }

    public int getViews(long id) {
        ResponseEntity<Object> response = get(String.format("/stats?start=%s&end=%s&unique=true&uris=/events/%s", formatter.format(LocalDateTime.now().minusYears(1000)), formatter.format(LocalDateTime.now()), id));

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JavaType type = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, StatsDto.class);

            List<StatsDto> body = objectMapper.convertValue(response.getBody(), type);

            if (body.isEmpty()) {
                return 0;
            } else {
                return body.getFirst().getHits();
            }
        }

        return 0;
    }

    public void sendViewToEvent(String ip, long eventId) {
        post("/hit",
                new HitDto(
                        "ewm-main-service",
                        "/events/" + eventId,
                        ip,
                        LocalDateTime.now()
                )
        );
    }

    public void sendViewToEvents(String ip) {
        post("/hit",
                new HitDto(
                        "ewm-main-service",
                        "/events",
                        ip,
                        LocalDateTime.now()
                )
        );
    }

}