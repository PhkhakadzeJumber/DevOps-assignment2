package ge.ticketebi.ticketebi_backend.services;

import ge.ticketebi.ticketebi_backend.domain.dto.CategoryDto;
import ge.ticketebi.ticketebi_backend.domain.entities.CategoryEntity;
import ge.ticketebi.ticketebi_backend.exceptions.InvalidRequestException;
import ge.ticketebi.ticketebi_backend.exceptions.ResourceNotFoundException;
import ge.ticketebi.ticketebi_backend.mappers.Mapper;
import ge.ticketebi.ticketebi_backend.repositories.CategoryRepository;
import ge.ticketebi.ticketebi_backend.services.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private Mapper<CategoryEntity, CategoryDto> categoryMapper;

    @InjectMocks private CategoryServiceImpl categoryService;

    private CategoryEntity categoryEntity;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        categoryEntity = new CategoryEntity();
        categoryEntity.setName("Football");

        categoryDto = new CategoryDto();
        categoryDto.setName("Football");
    }

    @Test
    void getCategories_shouldReturnMappedList() {
        when(categoryRepository.findAll()).thenReturn(List.of(categoryEntity));
        when(categoryMapper.mapTo(categoryEntity)).thenReturn(categoryDto);

        List<CategoryDto> result = categoryService.getCategories();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Football");
        verify(categoryRepository).findAll();
        verify(categoryMapper).mapTo(categoryEntity);
    }

    @Test
    void getCategoryByName_shouldReturnMappedEntity_whenFound() {
        when(categoryRepository.findByName("Football")).thenReturn(Optional.of(categoryEntity));
        when(categoryMapper.mapTo(categoryEntity)).thenReturn(categoryDto);

        CategoryDto result = categoryService.getCategoryByName("Football");

        assertThat(result.getName()).isEqualTo("Football");
        verify(categoryRepository).findByName("Football");
    }

    @Test
    void getCategoryByName_shouldThrow_whenNotFound() {
        when(categoryRepository.findByName("Unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryByName("Unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category with name Unknown not found");
    }

    @Test
    void createCategory_shouldMapAndSave() {
        when(categoryMapper.mapFrom(categoryDto)).thenReturn(categoryEntity);
        when(categoryRepository.save(categoryEntity)).thenReturn(categoryEntity);
        when(categoryMapper.mapTo(categoryEntity)).thenReturn(categoryDto);

        CategoryDto result = categoryService.createCategory(categoryDto);

        assertThat(result.getName()).isEqualTo("Football");
        verify(categoryRepository).save(categoryEntity);
    }
    @Test
    void createCategory_shouldThrow_whenExists() {
        when(categoryRepository.findByName("Football")).thenReturn(Optional.ofNullable(categoryEntity));

        assertThatThrownBy(() -> categoryService.createCategory(categoryDto))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Category already exists");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteCategory_shouldDelete_whenExists() {
        when(categoryRepository.findByName("Football")).thenReturn(Optional.of(categoryEntity));

        categoryService.deleteCategory("Football");

        verify(categoryRepository).delete(categoryEntity);
    }

    @Test
    void deleteCategory_shouldThrow_whenNotFound() {
        when(categoryRepository.findByName("Unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory("Unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCategory_shouldUpdateAndReturnDto() {
        CategoryDto updatedDto = new CategoryDto();
        updatedDto.setName("Basketball");

        CategoryEntity updatedEntity = new CategoryEntity();
        updatedEntity.setName("Basketball");

        when(categoryRepository.findByName("Football")).thenReturn(Optional.of(categoryEntity));
        when(categoryRepository.save(categoryEntity)).thenReturn(updatedEntity);
        when(categoryMapper.mapTo(updatedEntity)).thenReturn(updatedDto);

        CategoryDto result = categoryService.updateCategory("Football", updatedDto);

        assertThat(result.getName()).isEqualTo("Basketball");
        verify(categoryRepository).save(categoryEntity);
    }
}