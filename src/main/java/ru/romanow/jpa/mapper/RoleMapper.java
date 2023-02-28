package ru.romanow.jpa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.romanow.jpa.domain.Role;
import ru.romanow.jpa.repository.RoleRepository;

@Mapper(config = MapperConfiguration.class)
public abstract class RoleMapper {

    @Autowired
    private RoleRepository roleRepository;

    String toModel(Role role) {
        return role.getName();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    abstract Role toEntity(String name);

    @ObjectFactory
    public Role resolve(String name) {
        return roleRepository
                .findByName(name)
                .orElse(new Role().setName(name));
    }
}
