package ru.romanow.jpa.mapper;

import org.mapstruct.*;
import ru.romanow.jpa.domain.Authority;
import ru.romanow.jpa.model.AuthorityInfo;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(config = MapperConfiguration.class, uses = ReferenceMapper.class)
public interface AuthorityMapper {
    AuthorityInfo toModel(Authority grant);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "person", ignore = true)
    Authority toEntity(AuthorityInfo request);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void update(AuthorityInfo request, @MappingTarget Authority authority);

    @FullUpdate
    @InheritConfiguration(name = "toEntity")
    void fullUpdate(AuthorityInfo request, @MappingTarget Authority authority);
}
