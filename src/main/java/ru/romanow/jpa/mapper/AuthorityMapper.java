package ru.romanow.jpa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.romanow.jpa.domain.Authority;
import ru.romanow.jpa.model.AuthorityInfo;

@Mapper(config = MapperConfiguration.class)
public interface AuthorityMapper {
    AuthorityInfo toModel(Authority grant);
}
