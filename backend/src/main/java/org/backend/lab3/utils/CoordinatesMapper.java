package org.backend.lab3.utils;

import org.backend.lab3.coordinates.CoordinatesEntity;
import org.backend.lab3.coordinates.dto.CoordinatesResponse;
import org.springframework.stereotype.Component;

@Component
public class CoordinatesMapper {

    public CoordinatesResponse fromEntity(CoordinatesEntity coordinatesEntity) {
        return new CoordinatesResponse(
                coordinatesEntity.getId(),
                coordinatesEntity.getX(),
                coordinatesEntity.getY()
        );
    }
}
