package ru.romanow.jpa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.romanow.jpa.domain.Role;

@Mapper(config = MapperConfiguration.class)
public interface RoleMapper {

    default String toModel(Role role) {
        return role.getName();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "request")
    void update(String request, @MappingTarget Role role);
}
