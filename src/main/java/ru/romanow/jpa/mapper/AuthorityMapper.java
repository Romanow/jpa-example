package ru.romanow.jpa.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.romanow.jpa.domain.Authority;
import ru.romanow.jpa.model.AuthorityInfo;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(config = MapperConfiguration.class)
public interface AuthorityMapper {
    AuthorityInfo toModel(Authority grant);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "person", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void update(AuthorityInfo request, @MappingTarget Authority authority);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "person", ignore = true)
    void fullUpdate(AuthorityInfo request, @MappingTarget Authority authority);
}
