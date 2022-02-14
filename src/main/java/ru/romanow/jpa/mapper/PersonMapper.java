package ru.romanow.jpa.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.romanow.jpa.domain.Person;
import ru.romanow.jpa.domain.Role;
import ru.romanow.jpa.model.PersonModifyRequest;
import ru.romanow.jpa.model.PersonResponse;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(config = MapperConfiguration.class, uses = { AddressMapper.class, AuthorityMapper.class })
public interface PersonMapper {

    PersonResponse toModel(Person person);

    default String toModel(Role role) {
        return role.getName();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addressId", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void update(PersonModifyRequest request, @MappingTarget Person person);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addressId", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    void fullUpdate(PersonModifyRequest request, @MappingTarget Person person);
}
