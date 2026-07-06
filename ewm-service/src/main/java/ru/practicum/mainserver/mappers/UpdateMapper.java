package ru.practicum.mainserver.mappers;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;

@UtilityClass
public class UpdateMapper {

    public static  <T> void mergeObjects(T source, T target) {
        for (Field field : target.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            if (field.getName().equals("id")) {
                continue;
            }

            try {
                Object value = field.get(target);
                if (value != null) {
                    field.set(source, value);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
