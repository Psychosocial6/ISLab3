package org.backend.lab3.utils;

import org.backend.lab3.locations.LocationEntity;
import org.backend.lab3.locations.dto.LocationResponse;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {

    public LocationResponse fromEntity(LocationEntity locationEntity) {
        if (locationEntity == null) {
            return null;
        }

        return new LocationResponse(
                locationEntity.getId(),
                locationEntity.getX(),
                locationEntity.getY(),
                locationEntity.getZ()
        );
    }
}
