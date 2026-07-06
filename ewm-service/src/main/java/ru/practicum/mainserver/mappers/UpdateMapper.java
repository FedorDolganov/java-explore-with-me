package ru.practicum.mainserver.mappers;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;

@UtilityClass
public class UpdateMapper {

    public static  <T> void mergeObjects(T source, T target) {
        for (Field field : source.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(source);
                if (value != null) {
                    field.set(target, value);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
