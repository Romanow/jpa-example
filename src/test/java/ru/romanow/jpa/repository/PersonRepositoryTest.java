package ru.romanow.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.romanow.jpa.domain.Authority;
import ru.romanow.jpa.domain.Person;

import javax.persistence.EntityManager;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@DataJpaTest()
class PersonRepositoryTest {

    @Autowired
    private EntityManager em;

    @Test
    void shouldSavePersonWithAllAuthority() {
        final var savedPerson = makeSavedPersonWithAuthorities();

        assertThat(savedPerson).isNotNull();
        assertThat(savedPerson.getAuthorities()).isNotNull();
        assertThat(savedPerson.getAuthorities()).allMatch(Objects::nonNull);
    }


    @Test
    void shouldDeleteAllAuthoritiesWithPerson() {
        final var savedPerson = makeSavedPersonWithAuthorities();
        assertThat(getAuthorityCount()).isNotZero();

        em.remove(savedPerson);

        em.flush();
        em.clear();

        assertThat(getAuthorityCount()).isZero();
    }

    private Long getAuthorityCount() {
        return (Long) em.createQuery("select count(a) from Authority a").getSingleResult();
    }

    private Person makeSavedPersonWithAuthorities() {
        final var authority1 = new Authority()
                .setName("authority1")
                .setPriority(1);
        final var authority2 = new Authority()
                .setName("authority2")
                .setPriority(2);
        final var person = new Person()
                .setFirstName("firstName")
                .setLastName("lastName")
                .setAuthorities(Set.of(authority1, authority2));
        em.persist(person);
        em.flush();
        em.clear();
        return em.find(Person.class, person.getId());
    }
}