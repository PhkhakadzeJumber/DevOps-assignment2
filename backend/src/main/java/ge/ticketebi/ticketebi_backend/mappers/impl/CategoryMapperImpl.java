package ge.ticketebi.ticketebi_backend.mappers.impl;

import ge.ticketebi.ticketebi_backend.domain.dto.CategoryDto;
import ge.ticketebi.ticketebi_backend.domain.entities.CategoryEntity;
import ge.ticketebi.ticketebi_backend.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapperImpl implements Mapper<CategoryEntity, CategoryDto> {

    private final ModelMapper modelMapper;

    public CategoryMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public CategoryDto mapTo(CategoryEntity categoryEntity) {
        return modelMapper.map(categoryEntity, CategoryDto.class);
    }

    @Override
    public CategoryEntity mapFrom(CategoryDto categoryDto) {
        return modelMapper.map(categoryDto, CategoryEntity.class);
    }

}
