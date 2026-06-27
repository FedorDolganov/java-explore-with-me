package ru.practicum.statsserver.mappers;

import lombok.experimental.UtilityClass;
import ru.practicum.statsdto.HitDto;
import ru.practicum.statsserver.objects.Hit;

@UtilityClass
public class HitMapper {

    public static Hit to(HitDto hit) {
        return new Hit(
              0L,
                hit.getApp(),
                hit.getUri(),
                hit.getIp(),
                hit.getTimestamp()
        );
    }

}
