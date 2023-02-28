package ru.romanow.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import ru.romanow.jpa.domain.Authority;

public interface AuthorityRepository
        extends CrudRepository<Authority, Integer> {
}
