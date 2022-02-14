package ru.romanow.jpa.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.romanow.jpa.domain.Address;
import ru.romanow.jpa.model.AddressInfo;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(config = MapperConfiguration.class)
public interface AddressMapper {
    AddressInfo toModel(Address address);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void update(AddressInfo request, @MappingTarget Address person);

    @Mapping(target = "id", ignore = true)
    void fullUpdate(AddressInfo request, @MappingTarget Address person);
}
