package ru.romanow.jpa.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.romanow.jpa.domain.Authority;
import ru.romanow.jpa.domain.Person;
import ru.romanow.jpa.model.AuthorityInfo;
import ru.romanow.jpa.model.PersonModifyRequest;
import ru.romanow.jpa.repository.PersonRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.EmbeddedDatabaseConnection.H2;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase(connection = H2)
class PersonServiceImplTest {
    @Autowired
    private PersonService personService;
    @Autowired
    private PersonRepository personRepository;

    private Person person;

    @BeforeEach
    void fillDb() {
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
        this.person = this.personRepository.save(person);
    }

    @AfterEach
    void clearDb() {
        personRepository.delete(this.person);
    }

    @Test
    void shouldAddNewAuthorityWhenPersonIsFullUpdate() {
        final var initPersonResponse = personService.findById(this.person.getId());
        final var newAuthority = new AuthorityInfo()
                .setName("authority3")
                .setPriority(3);
        final List<AuthorityInfo> initAuthorities = initPersonResponse.getAuthorities();
        initAuthorities.add(newAuthority);
        final var request = new PersonModifyRequest()
                .setFirstName(initPersonResponse.getFirstName())
                .setLastName(initPersonResponse.getLastName())
                .setAuthorities(new HashSet<>(initAuthorities));

        final var personResponse = personService.fullUpdate(initPersonResponse.getId(), request);

        assertThat(personResponse).isNotNull();
        assertThat(personResponse.getAuthorities()).isNotNull().containsExactlyElementsOf(initAuthorities);
    }

    @Test
    void shouldUpdateExistedAuthorityWhenPersonIsFullUpdated() {
        final var initPersonResponse = personService.findById(this.person.getId());
        final var authorities = initPersonResponse.getAuthorities();
        authorities.get(0).setName("updatedName");

        final var request = new PersonModifyRequest()
                .setFirstName(initPersonResponse.getFirstName())
                .setLastName(initPersonResponse.getLastName())
                .setAuthorities(new HashSet<>(authorities));

        final var personResponse = personService.fullUpdate(initPersonResponse.getId(), request);

        assertThat(personResponse).isNotNull();
        assertThat(personResponse.getAuthorities()).isNotNull().containsExactlyElementsOf(authorities);
    }

    @Test
    void shouldDeleteExistedAuthorityWhenPersonIsFullUpdated() {
        final var initPersonResponse = personService.findById(this.person.getId());
        final var authorities = initPersonResponse.getAuthorities();
        authorities.remove(0);

        final var request = new PersonModifyRequest()
                .setFirstName(initPersonResponse.getFirstName())
                .setLastName(initPersonResponse.getLastName())
                .setAuthorities(new HashSet<>(authorities));

        final var personResponse = personService.fullUpdate(initPersonResponse.getId(), request);

        assertThat(personResponse).isNotNull();
        assertThat(personResponse.getAuthorities()).isNotNull().containsExactlyElementsOf(authorities);
    }
}