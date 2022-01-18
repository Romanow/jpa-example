package ru.romanow.jpa.mapper;

import org.mapstruct.Mapper;
import ru.romanow.jpa.domain.Person;
import ru.romanow.jpa.model.PersonResponse;

@Mapper(componentModel = "spring", uses = { AddressMapper.class })
public interface PersonMapper {
    PersonResponse toModel(Person person);
}
