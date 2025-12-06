package ru.romanow.jpa.mapper;

import org.mapstruct.*;
import ru.romanow.jpa.domain.Person;
import ru.romanow.jpa.mapper.config.MapperConfiguration;
import ru.romanow.jpa.mapper.utils.ReferenceMapper;
import ru.romanow.jpa.model.PersonModifyRequest;
import ru.romanow.jpa.model.PersonResponse;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(config = MapperConfiguration.class,
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        uses = { AddressMapper.class, AuthorityMapper.class, RoleMapper.class, ReferenceMapper.class })
public interface PersonMapper {

    PersonResponse toModel(Person person);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addressId", ignore = true)
    Person toEntity(PersonModifyRequest response);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void update(PersonModifyRequest request, @MappingTarget Person person);
}
