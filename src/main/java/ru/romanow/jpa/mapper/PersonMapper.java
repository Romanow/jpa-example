package ru.romanow.jpa.mapper;

import org.mapstruct.*;
import ru.romanow.jpa.domain.Person;
import ru.romanow.jpa.model.PersonModifyRequest;
import ru.romanow.jpa.model.PersonResponse;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(config = MapperConfiguration.class,
        uses = { AddressMapper.class, AuthorityMapper.class, RoleMapper.class, ReferenceMapper.class })
public interface PersonMapper {

    PersonResponse toModel(Person person);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addressId", ignore = true)
    Person toEntity(PersonModifyRequest response);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addressId", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void update(PersonModifyRequest request, @MappingTarget Person person);

    @InheritConfiguration(name = "toEntity")
    @Mapping(target = "address", qualifiedBy = FullUpdate.class)
    void fullUpdate(PersonModifyRequest request, @MappingTarget Person person);
}
