package ru.practicum.mainserver.categories.controllers;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainserver.categories.dto.CategoryDto;
import ru.practicum.mainserver.categories.services.CategoryService;

import java.util.List;

@RestController
@RequestMapping(path = "/categories")
@AllArgsConstructor
public class CategoriesController {

    @Autowired
    private CategoryService categoryService;


    @GetMapping
    public List<CategoryDto> getCategories(@RequestParam(required = false, defaultValue = "0") Integer from,
                                         @RequestParam(required = false, defaultValue = "10") Integer size) {
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategory(@PathVariable long catId) {
        return categoryService.getCategory(catId);
    }

}
