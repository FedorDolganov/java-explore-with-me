package ru.practicum.mainserver.categories.services;

import ru.practicum.mainserver.categories.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto getCategory(long catId);
}
