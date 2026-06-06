package ge.ticketebi.ticketebi_backend.services;

import ge.ticketebi.ticketebi_backend.domain.dto.LocationDto;

import java.util.List;

public interface LocationService {
    List<LocationDto> getLocations();
    LocationDto getLocationByName(String name);
    LocationDto createLocation(LocationDto location);
    void deleteLocation(String name);
    LocationDto updateLocation(String name, LocationDto locationDto);
}
