package org.backend.lab3.coordinates;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backend.lab3.coordinates.dto.CoordinatesResponse;
import org.backend.lab3.utils.CoordinatesMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class CoordinatesService {
    private final CoordinatesRepository coordinatesRepository;
    private final CoordinatesMapper coordinatesMapper;

    public List<CoordinatesResponse> getCoordinates() {
        return coordinatesRepository.findAvailableCoordinates()
                .stream()
                .map(coordinatesMapper::fromEntity)
                .toList();
    }

}
