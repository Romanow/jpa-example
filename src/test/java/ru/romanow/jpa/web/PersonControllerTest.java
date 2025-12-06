package ru.romanow.jpa.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.romanow.jpa.domain.Address;
import ru.romanow.jpa.domain.Authority;
import ru.romanow.jpa.domain.Person;
import ru.romanow.jpa.domain.Role;
import ru.romanow.jpa.model.AddressInfo;
import ru.romanow.jpa.model.AuthorityInfo;
import ru.romanow.jpa.model.PersonModifyRequest;
import ru.romanow.jpa.repository.PersonRepository;
import ru.romanow.jpa.web.config.DatabaseTestConfiguration;
import ru.romanow.jpa.web.dao.EntityDao;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Set.of;
import static java.util.stream.Stream.concat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.romanow.jpa.web.utils.EntityBuilder.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureTestEntityManager
@AutoConfigureMockMvc
@Import(DatabaseTestConfiguration.class)
class PersonControllerTest {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private EntityDao entityDao;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testPersons()
        throws Exception {
        personRepository.saveAll(
            List.of(buildPerson(ROLE_COUNT, AUTHORITY_COUNT), buildPerson(ROLE_COUNT, AUTHORITY_COUNT))
        );

        mockMvc.perform(get("/api/v1/persons"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testPersonById()
        throws Exception {
        var person = personRepository.save(buildPerson(ROLE_COUNT, AUTHORITY_COUNT));
        var address = person.getAddress();
        var roles = personRoles(person).toArray(String[]::new);
        var authorities = personAuthorities(person).toArray(String[]::new);

        mockMvc.perform(get("/api/v1/persons/{id}", person.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value(person.getFirstName()))
            .andExpect(jsonPath("$.middleName").value(person.getMiddleName()))
            .andExpect(jsonPath("$.lastName").value(person.getLastName()))
            .andExpect(jsonPath("$.age").value(person.getAge()))
            .andExpect(jsonPath("$.address.id").value(address.getId()))
            .andExpect(jsonPath("$.address.country").value(address.getCountry()))
            .andExpect(jsonPath("$.address.city").value(address.getCity()))
            .andExpect(jsonPath("$.address.street").value(address.getStreet()))
            .andExpect(jsonPath("$.address.address").value(address.getAddress()))
            .andExpect(jsonPath("$.roles[*]").value(containsInAnyOrder(roles)))
            .andExpect(jsonPath("$.authorities[*].name").value(containsInAnyOrder(authorities)));
    }

    @Test
    void testCreate()
        throws Exception {
        var request = buildPersonModifyRequest(ROLE_COUNT, AUTHORITY_COUNT);
        var location = mockMvc.perform(post("/api/v1/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getHeader(HttpHeaders.LOCATION);

        var id = StringUtils.substringAfterLast(location, "/");

        var address = request.getAddress();
        var roles = request.getRoles().toArray(String[]::new);
        var authorities = request.getAuthorities().stream().map(AuthorityInfo::getName).toArray(String[]::new);

        mockMvc.perform(get("/api/v1/persons/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value(request.getFirstName()))
            .andExpect(jsonPath("$.middleName").value(request.getMiddleName()))
            .andExpect(jsonPath("$.lastName").value(request.getLastName()))
            .andExpect(jsonPath("$.age").value(request.getAge()))
            .andExpect(jsonPath("$.address.country").value(address.getCountry()))
            .andExpect(jsonPath("$.address.city").value(address.getCity()))
            .andExpect(jsonPath("$.address.street").value(address.getStreet()))
            .andExpect(jsonPath("$.address.address").value(address.getAddress()))
            .andExpect(jsonPath("$.roles[*]").value(containsInAnyOrder(roles)))
            .andExpect(jsonPath("$.authorities[*].name").value(containsInAnyOrder(authorities)));

        assertThat(entityDao.findAll(Role.class))
            .extracting("name")
            .containsExactlyInAnyOrder(roles);

        assertThat(entityDao.findAll(Authority.class))
            .extracting("name")
            .containsExactlyInAnyOrder(authorities);
    }

    @Test
    void testUpdate()
        throws Exception {
        var person = personRepository.save(buildPerson(ROLE_COUNT, AUTHORITY_COUNT));
        var initialRoles = List.copyOf(person.getRoles());
        var initialAuthorities = List.copyOf(person.getAuthorities());
        var roles = of("Architect", "DevOps", initialRoles.get(1).getName());
        var request = new PersonModifyRequest()
            .setFirstName("Alex")
            .setLastName("Romanow")
            .setAddress(new AddressInfo().setStreet("Molostov st."))
            .setRoles(roles)
            .setAuthorities(of(
                new AuthorityInfo().setId(initialAuthorities.get(0).getId()).setName("EAT").setPriority(1),
                new AuthorityInfo().setId(initialAuthorities.get(1).getId()).setName("SLEEP").setPriority(2),
                new AuthorityInfo().setName("RIDE").setPriority(3)
            ));

        mockMvc.perform(patch("/api/v1/persons/{id}", person.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value(request.getFirstName()))
            .andExpect(jsonPath("$.middleName").value(person.getMiddleName()))
            .andExpect(jsonPath("$.lastName").value(request.getLastName()))
            .andExpect(jsonPath("$.age").value(person.getAge()))
            .andExpect(jsonPath("$.address.country").value(person.getAddress().getCountry()))
            .andExpect(jsonPath("$.address.city").value(person.getAddress().getCity()))
            .andExpect(jsonPath("$.address.street").value(request.getAddress().getStreet()))
            .andExpect(jsonPath("$.address.address").value(person.getAddress().getAddress()))
            .andExpect(jsonPath("$.roles[*]")
                .value(
                    containsInAnyOrder(
                        "Architect",
                        "DevOps",
                        initialRoles.get(0).getName(),
                        initialRoles.get(1).getName()
                    )
                )
            )
            .andExpect(jsonPath("$.authorities[*].name")
                .value(
                    containsInAnyOrder(
                        "EAT", "SLEEP", "RIDE", initialAuthorities.get(2).getName()
                    )
                )
            );

        assertThat(entityDao.findAll(Role.class))
            .extracting("name")
            .containsExactlyInAnyOrder("Architect", "DevOps", initialRoles.get(0).getName(), initialRoles.get(1).getName());
    }

    @Test
    void testFullUpdate()
        throws Exception {
        var person = personRepository.save(buildPerson(ROLE_COUNT, AUTHORITY_COUNT));
        var roles = of("Architect", "DevOps", "Developer");
        var initialRoles = personRoles(person).toArray(String[]::new);
        var initialAuthorities = List.copyOf(person.getAuthorities());
        var request = buildPersonModifyRequest(ROLE_COUNT, AUTHORITY_COUNT)
            .setFirstName("Alex")
            .setMiddleName(null)
            .setLastName("Romanow")
            .setAddress(new AddressInfo()
                .setCountry("USA")
                .setCity("NY")
                .setAddress("Molostovih st."))
            .setRoles(roles)
            .setAuthorities(of(
                new AuthorityInfo().setId(initialAuthorities.get(0).getId()).setName("EAT").setPriority(1),
                new AuthorityInfo().setId(initialAuthorities.get(1).getId()).setName("SLEEP").setPriority(2),
                new AuthorityInfo().setName("RIDE").setPriority(3)
            ));

        mockMvc.perform(put("/api/v1/persons/{id}", person.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value(request.getFirstName()))
            .andExpect(jsonPath("$.middleName").value(request.getMiddleName()))
            .andExpect(jsonPath("$.lastName").value(request.getLastName()))
            .andExpect(jsonPath("$.age").value(request.getAge()))
            .andExpect(jsonPath("$.address.country").value(request.getAddress().getCountry()))
            .andExpect(jsonPath("$.address.city").value(request.getAddress().getCity()))
            .andExpect(jsonPath("$.address.street").value(request.getAddress().getStreet()))
            .andExpect(jsonPath("$.address.address").value(request.getAddress().getAddress()))
            .andExpect(jsonPath("$.roles[*]").value(containsInAnyOrder("Architect", "DevOps", "Developer")))
            .andExpect(jsonPath("$.authorities[*].name").value(containsInAnyOrder("EAT", "SLEEP", "RIDE")));

        assertThat(entityDao.findAll(Role.class))
            .extracting("name")
            .containsExactlyInAnyOrder(concat(stream(initialRoles), roles.stream()).toArray());

        assertThat(entityDao.findAll(Authority.class))
            .extracting("id", "name", "priority")
            .containsExactlyInAnyOrder(tuple(1, "EAT", 1), tuple(2, "SLEEP", 2), tuple(4, "RIDE", 3));

        assertThat(entityDao.findById(initialAuthorities.get(2).getId(), Authority.class).isEmpty()).isTrue();
    }

    @Test
    void testDelete()
        throws Exception {
        var person = personRepository.save(buildPerson(ROLE_COUNT, AUTHORITY_COUNT));

        mockMvc.perform(delete("/api/v1/persons/{id}", person.getId()))
            .andExpect(status().isNoContent());

        assertThat(personRepository.findById(person.getId()).isPresent()).isFalse();
        assertThat(entityDao.findById(person.getAddress().getId(), Address.class).isPresent()).isFalse();
        assertThat(entityDao.findAll(Role.class))
            .extracting("name")
            .containsExactlyInAnyOrder(personRoles(person).toArray());
        assertThat(entityDao.findAll(Authority.class)).isEmpty();
    }

    @NotNull
    private Stream<String> personRoles(Person person) {
        return person.getRoles().stream().map(Role::getName);
    }

    @NotNull
    private Stream<String> personAuthorities(Person person) {
        return person.getAuthorities().stream().map(Authority::getName);
    }

}
