package org.backend.lab3.persons.dto;

import org.backend.lab3.coordinates.dto.CoordinatesResponse;
import org.backend.lab3.enums.Color;
import org.backend.lab3.enums.Country;
import org.backend.lab3.locations.dto.LocationResponse;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public record PersonResponse(
        Long id,
        String name,
        CoordinatesResponse coordinates,
        LocalDateTime creationDate,
        Color eyeColor,
        Color hairColor,
        LocationResponse location,
        Double height,
        ZonedDateTime birthday,
        Double weight,
        Country nationality
) {
}
