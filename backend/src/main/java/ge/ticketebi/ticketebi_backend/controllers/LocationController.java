package ge.ticketebi.ticketebi_backend.controllers;

import ge.ticketebi.ticketebi_backend.domain.dto.LocationDto;
import ge.ticketebi.ticketebi_backend.domain.dto.MessageResponse;
import ge.ticketebi.ticketebi_backend.services.LocationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public ResponseEntity<List<LocationDto>> getLocations() {
        List<LocationDto> locations = locationService.getLocations();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{name}")
    public ResponseEntity<LocationDto> getLocationByName(@PathVariable String name) {
        LocationDto location = locationService.getLocationByName(name);
        return ResponseEntity.ok(location);
    }

    @PostMapping
    public ResponseEntity<LocationDto> createLocation(@RequestBody LocationDto dto) {
        LocationDto created = locationService.createLocation(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<MessageResponse> deleteLocation(@PathVariable String name) {
        locationService.deleteLocation(name);
        return ResponseEntity.ok(new MessageResponse("Location " + name + "deleted successfully"));
    }

    @PutMapping("/{name}")
    public ResponseEntity<LocationDto> updateLocation(
            @PathVariable String name,
            @RequestBody LocationDto locationDto
    ) {
        LocationDto updatedDto = locationService.updateLocation(name, locationDto);
        return ResponseEntity.ok(updatedDto);
    }

}
