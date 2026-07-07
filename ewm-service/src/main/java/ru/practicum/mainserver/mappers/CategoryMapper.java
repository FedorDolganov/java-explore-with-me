package ru.practicum.mainserver.mappers;

import lombok.experimental.UtilityClass;
import ru.practicum.mainserver.categories.Category;
import ru.practicum.mainserver.categories.dto.CategoryDto;
import ru.practicum.mainserver.categories.dto.NewCategoryDto;

@UtilityClass
public class CategoryMapper {

    public static CategoryDto toDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName()
        );
    }

    public static Category to(NewCategoryDto categoryDto) {
        return new Category(
                0L,
                categoryDto.getName()
        );
    }
}
