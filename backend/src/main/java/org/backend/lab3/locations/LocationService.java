package org.backend.lab3.locations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backend.lab3.cache.TrackCache;
import org.backend.lab3.locations.dto.LocationResponse;
import org.backend.lab3.utils.LocationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class LocationService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @TrackCache
    public List<LocationResponse> getLocations() {
        return locationRepository.findAvailableLocations()
                .stream()
                .map(locationMapper::fromEntity)
                .toList();
    }
}
