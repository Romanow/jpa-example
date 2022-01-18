package ru.romanow.jpa.mapper;

import org.mapstruct.Mapper;
import ru.romanow.jpa.domain.Address;
import ru.romanow.jpa.model.AddressInfo;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    AddressInfo toModel(Address address);
}
