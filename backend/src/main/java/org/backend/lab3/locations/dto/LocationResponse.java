package org.backend.lab3.locations.dto;

public record LocationResponse(
        Long id,
        Double x,
        Long y,
        Double z
) {
}
