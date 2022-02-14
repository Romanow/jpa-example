package ru.romanow.jpa.web;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.romanow.jpa.domain.Address;
import ru.romanow.jpa.domain.Authority;
import ru.romanow.jpa.domain.Role;
import ru.romanow.jpa.model.AddressInfo;
import ru.romanow.jpa.model.AuthorityInfo;
import ru.romanow.jpa.model.PersonModifyRequest;
import ru.romanow.jpa.repository.PersonRepository;
import ru.romanow.jpa.web.dao.EntityDao;

import java.util.List;

import static java.util.Set.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.boot.jdbc.EmbeddedDatabaseConnection.H2;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.romanow.jpa.web.utils.EntityBuilder.buildPerson;
import static ru.romanow.jpa.web.utils.EntityBuilder.buildPersonModifyRequest;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase(connection = H2)
@AutoConfigureMockMvc
class PersonControllerTest {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private EntityDao entityDao;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testPersons()
            throws Exception {
        personRepository.saveAll(List.of(buildPerson(3, 2), buildPerson(2, 2)));

        mockMvc.perform(get("/api/v1/persons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testPersonById()
            throws Exception {
        var person = personRepository.save(buildPerson(2, 2));
        var address = person.getAddress();
        var roles = person.getRoles().stream().map(Role::getName).toArray(String[]::new);
        var authorities = person.getAuthorities().stream().map(Authority::getName).toArray(String[]::new);

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
        var gson = new Gson();
        var request = buildPersonModifyRequest(2);
        var location = mockMvc.perform(post("/api/v1/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(request)))
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
    }

    @Test
    void testUpdate()
            throws Exception {
        var gson = new Gson();
        var person = personRepository.save(buildPerson(2, 3));
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
                        .content(gson.toJson(request)))
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
    }

    @Test
    void testFullUpdate()
            throws Exception {
        var gson = new Gson();
        var person = personRepository.save(buildPerson(2, 3));
        var roles = of("Architect", "DevOps", "Developer");
        var initialAuthorities = List.copyOf(person.getAuthorities());
        var request = buildPersonModifyRequest(2)
                .setFirstName("Alex")
                .setMiddleName(null)
                .setLastName("Romanow")
                .setAddress(new AddressInfo()
                        .setCountry("USA")
                        .setCity("NY")
                        .setAddress("Molostov st."))
                .setRoles(roles)
                .setAuthorities(of(
                        new AuthorityInfo().setId(initialAuthorities.get(0).getId()).setName("EAT").setPriority(1),
                        new AuthorityInfo().setId(initialAuthorities.get(1).getId()).setName("SLEEP").setPriority(2),
                        new AuthorityInfo().setName("RIDE").setPriority(3)
                ));;

        mockMvc.perform(put("/api/v1/persons/{id}", person.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(request)))
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

        for (var role : person.getRoles()) {
            assertThat(entityDao.findById(role.getId(), Role.class).isPresent()).isTrue();
        }
        for (int i = 0; i < 2; i++) {
            assertThat(entityDao.findById(initialAuthorities.get(i).getId(), Authority.class).isPresent()).isTrue();
        }
        assertThat(entityDao.findById(initialAuthorities.get(2).getId(), Authority.class).isPresent()).isFalse();
    }

    @Test
    void testDelete()
            throws Exception {
        var person = personRepository.save(buildPerson(2, 2));

        mockMvc.perform(delete("/api/v1/persons/{id}", person.getId()))
                .andExpect(status().isNoContent());

        assertThat(personRepository.findById(person.getId()).isPresent()).isFalse();
        assertThat(entityDao.findById(person.getAddress().getId(), Address.class).isPresent()).isFalse();
        for (var role : person.getRoles()) {
            assertThat(entityDao.findById(role.getId(), Role.class).isPresent()).isTrue();
        }
        for (var grant : person.getAuthorities()) {
            assertThat(entityDao.findById(grant.getId(), Authority.class).isPresent()).isFalse();
        }
    }
}