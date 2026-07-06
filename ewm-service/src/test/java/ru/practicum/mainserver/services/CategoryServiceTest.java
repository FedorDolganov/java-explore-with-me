package ru.practicum.mainserver.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.mainserver.categories.Category;
import ru.practicum.mainserver.categories.dto.CategoryDto;
import ru.practicum.mainserver.categories.repositories.CategoryRepository;
import ru.practicum.mainserver.categories.services.CategoryServiceImpl;
import ru.practicum.mainserver.exceptions.NotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private Category category2;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Test Category");

        category2 = new Category();
        category2.setId(2L);
        category2.setName("Test Category 2");
    }



    @Test
    void getCategories_ShouldReturnListOfCategoryDto() {
        when(categoryRepository.findByFromAndSize(anyInt(), anyInt()))
                .thenReturn(List.of(category, category2));

        List<CategoryDto> result = categoryService.getCategories(0, 10);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Category", result.get(0).getName());
        assertEquals("Test Category 2", result.get(1).getName());
        verify(categoryRepository, times(1)).findByFromAndSize(0, 10);
    }

    @Test
    void getCategories_WhenEmptyList_ShouldReturnEmptyList() {
        when(categoryRepository.findByFromAndSize(anyInt(), anyInt()))
                .thenReturn(List.of());

        List<CategoryDto> result = categoryService.getCategories(0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(categoryRepository, times(1)).findByFromAndSize(0, 10);
    }

    @Test
    void getCategories_WithDifferentPagination_ShouldPassParameters() {
        when(categoryRepository.findByFromAndSize(5, 15))
                .thenReturn(List.of(category));

        List<CategoryDto> result = categoryService.getCategories(5, 15);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(categoryRepository, times(1)).findByFromAndSize(5, 15);
    }

    @Test
    void getCategory_WhenCategoryExists_ShouldReturnCategoryDto() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryDto result = categoryService.getCategory(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Category", result.getName());
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void getCategory_WhenCategoryNotFound_ShouldThrowNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> categoryService.getCategory(99L)
        );

        assertTrue(exception.getMessage().contains("Category with id=99 not found."));
        verify(categoryRepository, times(1)).findById(99L);
    }

    @Test
    void getCategory_WithDifferentExistingId_ShouldReturnCorrectCategory() {
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category2));

        CategoryDto result = categoryService.getCategory(2L);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Test Category 2", result.getName());
        verify(categoryRepository, times(1)).findById(2L);
    }

    @Test
    void getCategory_WithZeroId_ShouldThrowNotFoundException() {
        when(categoryRepository.findById(0L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> categoryService.getCategory(0L)
        );

        assertTrue(exception.getMessage().contains("Category with id=0 not found."));
        verify(categoryRepository, times(1)).findById(0L);
    }

    @Test
    void getCategory_WithNegativeId_ShouldThrowNotFoundException() {
        when(categoryRepository.findById(-1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> categoryService.getCategory(-1L)
        );

        assertTrue(exception.getMessage().contains("Category with id=-1 not found."));
        verify(categoryRepository, times(1)).findById(-1L);
    }
}