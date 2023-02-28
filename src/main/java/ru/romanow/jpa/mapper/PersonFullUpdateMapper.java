package ru.romanow.jpa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.romanow.jpa.domain.Person;
import ru.romanow.jpa.mapper.config.MapperConfiguration;
import ru.romanow.jpa.mapper.utils.FullUpdate;
import ru.romanow.jpa.mapper.utils.ReferenceMapper;
import ru.romanow.jpa.model.PersonModifyRequest;

@Mapper(config = MapperConfiguration.class,
        uses = { AddressMapper.class, AuthorityMapper.class, RoleMapper.class, ReferenceMapper.class })
public interface PersonFullUpdateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addressId", ignore = true)
    @Mapping(target = "address", qualifiedBy = FullUpdate.class)
    void fullUpdate(PersonModifyRequest request, @MappingTarget Person person);
}
