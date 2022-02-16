package ru.romanow.jpa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.romanow.jpa.domain.Role;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;

@Mapper(config = MapperConfiguration.class)
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = ".")
    Role toEntity(String role);

    default Set<Role> fullUpdateImpl(Set<Role> newRoles, @MappingTarget Set<Role> roles) {
        var newRoleMap = newRoles
                .stream()
                .filter(role -> Objects.nonNull(role.getId()))
                .collect(toMap(Role::getId, identity()));
        final var difference = roles.stream()
                .filter(role -> !newRoleMap.containsKey(role.getId()))
                .collect(Collectors.toSet());
        roles.removeAll(difference);

        var roleMap = roles
                .stream()
                .collect(toMap(Role::getId, identity()));
        for (var newRole : newRoles) {
            Optional.ofNullable(roleMap.get(newRole.getId()))
                    .ifPresentOrElse(
                            role -> fullUpdate(newRole, role),
                            () -> {
                                final var role = fullUpdate(newRole, new Role());
                                roles.add(role);
                            });
        }
        return roles;
    }

    @Mapping(target = "id", ignore = true)
    Role fullUpdate(Role newRole, @MappingTarget Role role);
}