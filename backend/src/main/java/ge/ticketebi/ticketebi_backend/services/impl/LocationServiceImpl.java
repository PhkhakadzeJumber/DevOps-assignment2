package ge.ticketebi.ticketebi_backend.services.impl;

import ge.ticketebi.ticketebi_backend.domain.dto.LocationDto;
import ge.ticketebi.ticketebi_backend.domain.entities.LocationEntity;
import ge.ticketebi.ticketebi_backend.exceptions.ResourceNotFoundException;
import ge.ticketebi.ticketebi_backend.mappers.Mapper;
import ge.ticketebi.ticketebi_backend.repositories.LocationRepository;
import ge.ticketebi.ticketebi_backend.services.LocationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final Mapper<LocationEntity, LocationDto> locationMapper;

    public LocationServiceImpl(
            LocationRepository locationRepository,
            Mapper<LocationEntity, LocationDto> locationMapper)
    {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
    }
    @Override
    public List<LocationDto> getLocations() {
        List<LocationEntity> locations = locationRepository.findAll();
        return locations.stream()
                .map(locationMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public LocationDto getLocationByName(String name) {
        LocationEntity entity = locationRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Location with name " + name + " not found"));
        return locationMapper.mapTo(entity);
    }

    @Override
    public LocationDto createLocation(LocationDto location) {
        LocationEntity entity = locationMapper.mapFrom(location);
        LocationEntity saved = locationRepository.save(entity);
        return locationMapper.mapTo(saved);
    }

    @Override
    public void deleteLocation(String name) {
        LocationEntity location = locationRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Location with name " + name + " not found"));
        locationRepository.delete(location);
    }

    @Override
    public LocationDto updateLocation(String name, LocationDto locationDto) {
        LocationEntity prev = locationRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Location with name" + name + "not found"));
        prev.setName(locationDto.getName());
        LocationEntity updated = locationRepository.save(prev);
        return locationMapper.mapTo(updated);
    }

}
