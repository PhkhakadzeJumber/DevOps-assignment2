package ge.ticketebi.ticketebi_backend.mappers.impl;

import ge.ticketebi.ticketebi_backend.domain.dto.CategoryDto;
import ge.ticketebi.ticketebi_backend.domain.dto.LocationDto;
import ge.ticketebi.ticketebi_backend.domain.entities.CategoryEntity;
import ge.ticketebi.ticketebi_backend.domain.entities.LocationEntity;
import ge.ticketebi.ticketebi_backend.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class LocationMapperImpl implements Mapper<LocationEntity, LocationDto>  {

    private final ModelMapper modelMapper;

    public LocationMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public LocationDto mapTo(LocationEntity locationEntity) {
        return modelMapper.map(locationEntity, LocationDto.class);
    }

    @Override
    public LocationEntity mapFrom(LocationDto locationDto) {
        return modelMapper.map(locationDto, LocationEntity.class);
    }

}
