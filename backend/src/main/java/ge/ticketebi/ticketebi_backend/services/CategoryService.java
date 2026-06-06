package ge.ticketebi.ticketebi_backend.services;

import ge.ticketebi.ticketebi_backend.domain.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getCategories();
    CategoryDto getCategoryByName(String name);
    CategoryDto createCategory(CategoryDto category);
    void deleteCategory(String name);
    CategoryDto updateCategory(String name, CategoryDto categoryDto);
}
