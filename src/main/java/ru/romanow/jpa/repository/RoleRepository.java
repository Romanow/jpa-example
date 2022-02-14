package ru.romanow.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import ru.romanow.jpa.domain.Role;

import java.util.Optional;

public interface RoleRepository
        extends CrudRepository<Role, Integer> {
    Optional<Role> findByName(String name);
}
