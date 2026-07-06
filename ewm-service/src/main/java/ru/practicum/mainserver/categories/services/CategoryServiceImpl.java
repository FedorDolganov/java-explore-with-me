package ru.practicum.mainserver.categories.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.mainserver.categories.Category;
import ru.practicum.mainserver.categories.dto.CategoryDto;
import ru.practicum.mainserver.categories.repositories.CategoryRepository;
import ru.practicum.mainserver.exceptions.NotFoundException;
import ru.practicum.mainserver.mappers.CategoryMapper;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;



    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        return categoryRepository.findByFromAndSize(from, size).stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto getCategory(long catId) {
        Optional<Category> category = categoryRepository.findById(catId);

        if (category.isEmpty()) {
            throw new NotFoundException(String.format("Category with id=%s not found.", catId));
        }

        return CategoryMapper.toDto(category.get());
    }
}
