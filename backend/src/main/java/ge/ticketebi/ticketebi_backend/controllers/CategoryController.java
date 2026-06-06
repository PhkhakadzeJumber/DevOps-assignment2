package ge.ticketebi.ticketebi_backend.controllers;

import ge.ticketebi.ticketebi_backend.domain.dto.CategoryDto;
import ge.ticketebi.ticketebi_backend.domain.dto.MessageResponse;
import ge.ticketebi.ticketebi_backend.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories() {
        List<CategoryDto> categories = categoryService.getCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{name}")
    public ResponseEntity<CategoryDto> getCategoryByName(@PathVariable String name) {
        CategoryDto category = categoryService.getCategoryByName(name);
        return ResponseEntity.ok(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody @Valid CategoryDto dto) {
        CategoryDto created = categoryService.createCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{name}")
    public ResponseEntity<MessageResponse> deleteCategory(@PathVariable String name) {
        categoryService.deleteCategory(name);
        return ResponseEntity.ok(new MessageResponse("Category "+ name + " deleted successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{name}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable String name,
            @RequestBody @Valid CategoryDto categoryDto
    ) {
        CategoryDto updatedDto = categoryService.updateCategory(name, categoryDto);
        return ResponseEntity.ok(updatedDto);
    }

}
