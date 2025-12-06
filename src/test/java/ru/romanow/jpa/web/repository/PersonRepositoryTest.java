package ru.romanow.jpa.web.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.romanow.jpa.domain.Authority;
import ru.romanow.jpa.repository.AuthorityRepository;
import ru.romanow.jpa.repository.PersonRepository;
import ru.romanow.jpa.web.config.DatabaseTestConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.romanow.jpa.web.utils.EntityBuilder.*;

@DataJpaTest
@Import(DatabaseTestConfiguration.class)
class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Test
    void shouldSavePersonWithAllAuthority() {
        final var person = buildPerson(ROLE_COUNT, AUTHORITY_COUNT);
        final var authorities = person
            .getAuthorities()
            .stream()
            .map(Authority::getName)
            .toArray(String[]::new);
        final var saved = personRepository.save(person);

        assertThat(saved).isNotNull();
        assertThat(saved.getAuthorities()).hasSize(3);
        assertThat(saved.getAuthorities()).extracting("name").containsExactlyInAnyOrder(authorities);
    }

    @Test
    void shouldDeleteAllAuthoritiesWithPerson() {
        final var person = personRepository.save(buildPerson(ROLE_COUNT, AUTHORITY_COUNT));
        assertThat(authorityRepository.count()).isNotZero();

        personRepository.delete(person);
        personRepository.flush();

        assertThat(authorityRepository.count()).isZero();
    }
}
