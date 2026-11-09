package org.backend.lab3.utils;

import lombok.RequiredArgsConstructor;
import org.backend.lab3.persons.PersonEntity;
import org.backend.lab3.persons.dto.PersonResponse;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PersonMapper {
    private final CoordinatesMapper coordinatesMapper;
    private final LocationMapper locationMapper;

    public PersonResponse fromEntity(PersonEntity personEntity) {
        return new PersonResponse(
                personEntity.getId(),
                personEntity.getName(),
                coordinatesMapper.fromEntity(personEntity.getCoordinates()),
                personEntity.getCreationDate(),
                personEntity.getEyeColor(),
                personEntity.getHairColor(),
                locationMapper.fromEntity(personEntity.getLocation()),
                personEntity.getHeight(),
                personEntity.getBirthday(),
                personEntity.getWeight(),
                personEntity.getNationality()
        );
    }
}
