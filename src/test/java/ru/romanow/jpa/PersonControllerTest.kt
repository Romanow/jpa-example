package ru.romanow.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.StringUtils.substringAfterLast
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.*
import org.springframework.transaction.annotation.Transactional
import ru.romanow.jpa.config.DatabaseTestConfiguration
import ru.romanow.jpa.dao.EntityDao
import ru.romanow.jpa.domain.Address
import ru.romanow.jpa.domain.Authority
import ru.romanow.jpa.domain.Role
import ru.romanow.jpa.model.AddressInfo
import ru.romanow.jpa.model.AuthorityInfo
import ru.romanow.jpa.model.PersonModifyRequest
import ru.romanow.jpa.repository.PersonRepository
import ru.romanow.jpa.utils.AUTHORITY_COUNT
import ru.romanow.jpa.utils.ROLE_COUNT
import ru.romanow.jpa.utils.buildPerson
import ru.romanow.jpa.utils.buildPersonModifyRequest

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureTestEntityManager
@AutoConfigureMockMvc
@Import(value = [DatabaseTestConfiguration::class])
internal class PersonControllerTest {
    @Autowired
    private lateinit var personRepository: PersonRepository

    @Autowired
    private lateinit var entityDao: EntityDao

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `when get all Persons then return only one record`() {
        personRepository.deleteAll()
        personRepository.saveAll(
            listOf(buildPerson(ROLE_COUNT, AUTHORITY_COUNT), buildPerson(ROLE_COUNT, AUTHORITY_COUNT))
        )
        mockMvc.get("/api/v1/persons")
            .andExpect {
                status { isOk() }
                status { jsonPath("$.length()") { value(2) } }
            }
    }

    @Test
    fun `when get Person by Id then successfully return all information`() {
        val person = personRepository.save(buildPerson(ROLE_COUNT, AUTHORITY_COUNT))
        val roles = person.roles.map { it.name }.toTypedArray()
        val authorities = person.authorities.map { it.name }.toTypedArray()
        mockMvc.get("/api/v1/persons/${person.id}")
            .andExpect {
                status { isOk() }
                jsonPath("$.firstName") { person.firstName }
                jsonPath("$.middleName") { person.middleName }
                jsonPath("$.lastName") { person.lastName }
                jsonPath("$.age") { person.age }
                jsonPath("$.address.id") { person.address?.id }
                jsonPath("$.address.country") { person.address?.country }
                jsonPath("$.address.city") { person.address?.city }
                jsonPath("$.address.street") { person.address?.street }
                jsonPath("$.address.address") { person.address?.address }
                jsonPath("$.roles[*]") { containsInAnyOrder(*roles) }
                jsonPath("$.authorities[*].name") { containsInAnyOrder(*authorities) }
            }
    }

    @Test
    fun `when create Person then successfully create Person, Address, Roles and Authorities`() {
        val request = buildPersonModifyRequest(ROLE_COUNT, AUTHORITY_COUNT)
        val location = mockMvc.post("/api/v1/persons") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
            .andExpect { status { isCreated() } }
            .andReturn()
            .response
            .getHeader(HttpHeaders.LOCATION)

        val id = substringAfterLast(location, "/")
        val roles = request.roles!!.toTypedArray()
        val authorities = request.authorities!!.map { it.name }.toTypedArray()
        mockMvc.get("/api/v1/persons/$id")
            .andExpect {
                status { isOk() }
                jsonPath("$.firstName") { request.firstName }
                jsonPath("$.middleName") { request.middleName }
                jsonPath("$.lastName") { request.lastName }
                jsonPath("$.age") { request.age }
                jsonPath("$.address.id") { request.address?.id }
                jsonPath("$.address.country") { request.address?.country }
                jsonPath("$.address.city") { request.address?.city }
                jsonPath("$.address.street") { request.address?.street }
                jsonPath("$.address.address") { request.address?.address }
                jsonPath("$.roles[*]") { containsInAnyOrder(*roles) }
                jsonPath("$.authorities[*].name") { containsInAnyOrder(*authorities) }
            }

        assertThat(entityDao.findAll(Role::class.java))
            .extracting("name")
            .containsExactlyInAnyOrder(*roles)

        assertThat(entityDao.findAll(Authority::class.java))
            .extracting("name")
            .containsExactlyInAnyOrder(*authorities)
    }

    @Test
    fun `when partial update Person then successfully update Person, add Roles and Authorities`() {
        val person = personRepository.save(buildPerson(ROLE_COUNT, AUTHORITY_COUNT))
        val roles = person.roles.toList()
        val authorities = person.authorities.toList()
        val request = PersonModifyRequest(
            firstName = "Alex",
            lastName = "Romanow",
            address = AddressInfo(street = "Molostov st."),
            roles = setOf("Architect", "DevOps", roles[1].name!!),
            authorities = setOf(
                AuthorityInfo(id = authorities[0].id, name = "EAT", priority = 1),
                AuthorityInfo(id = authorities[1].id, name = "SLEEP", priority = 1),
                AuthorityInfo(name = "RIDE", priority = 3)
            )
        )

        val expectedRoles = arrayOf("Architect", "DevOps", *roles.map { it.name }.toTypedArray())
        val expectedAuthorities = arrayOf("EAT", "SLEEP", "RIDE", authorities[2].name)
        mockMvc.patch("/api/v1/persons/${person.id}") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.firstName") { request.firstName }
                jsonPath("$.middleName") { person.middleName }
                jsonPath("$.lastName") { request.lastName }
                jsonPath("$.age") { person.age }
                jsonPath("$.address.id") { person.address?.id }
                jsonPath("$.address.country") { person.address?.country }
                jsonPath("$.address.city") { person.address?.city }
                jsonPath("$.address.street") { request.address?.street }
                jsonPath("$.address.address") { person.address?.address }
                jsonPath("$.roles[*]") { containsInAnyOrder(*expectedRoles) }
                jsonPath("$.authorities[*].name") { containsInAnyOrder(*expectedAuthorities) }
            }

        assertThat(entityDao.findAll(Role::class.java))
            .extracting("name")
            .containsExactlyInAnyOrder(*expectedRoles)
    }

    @Test
    fun `when full update person then successfully update Person, add Roles and upsert Authorities`() {
        val person = personRepository.save(buildPerson(ROLE_COUNT, AUTHORITY_COUNT))
        val roles = person.roles.toList()
        val authorities = person.authorities.toList()
        val request = PersonModifyRequest(
            firstName = "Alex",
            lastName = "Romanow",
            address = AddressInfo(country = "Armenia", city = "Yerevan", street = "Moldovokan st.", address = "43/4"),
            roles = setOf("Architect", "DevOps", "Developer"),
            authorities = setOf(
                AuthorityInfo(id = authorities[0].id, name = "EAT", priority = 1),
                AuthorityInfo(id = authorities[1].id, name = "SLEEP", priority = 1),
                AuthorityInfo(name = "RIDE", priority = 3)
            )
        )

        val expectedRoles = arrayOf("Architect", "DevOps", "Developer")
        val expectedAuthorities = arrayOf("EAT", "SLEEP", "RIDE")
        mockMvc.put("/api/v1/persons/${person.id}") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.firstName") { request.firstName }
                jsonPath("$.middleName") { request.middleName }
                jsonPath("$.lastName") { request.lastName }
                jsonPath("$.age") { request.age }
                jsonPath("$.address.country") { request.address?.country }
                jsonPath("$.address.city") { request.address?.city }
                jsonPath("$.address.street") { request.address?.street }
                jsonPath("$.address.address") { request.address?.address }
                jsonPath("$.roles[*]") { containsInAnyOrder(*expectedRoles) }
                jsonPath("$.authorities[*].name") { containsInAnyOrder(*expectedAuthorities) }
            }

        assertThat(entityDao.findAll(Role::class.java))
            .extracting("name")
            .containsExactlyInAnyOrder(*(roles.map { it.name } + expectedRoles).toTypedArray())

        assertThat(entityDao.findAll(Authority::class.java))
            .extracting("name")
            .containsExactlyInAnyOrder(*expectedAuthorities)

        assertThat(entityDao.findById(authorities[2].id!!, Authority::class.java)).isEmpty()
    }

    @Test
    fun `when delete Person then successfully delete Person, Roles, Address and Authorities`() {
        val person = personRepository.save(buildPerson(ROLE_COUNT, AUTHORITY_COUNT))
        mockMvc.delete("/api/v1/persons/${person.id}")
            .andExpect { status { isNoContent() } }

        val roles = person.roles.map { it.name }.toTypedArray()
        assertThat(personRepository.findById(person.id!!)).isEmpty
        assertThat(entityDao.findById(person.address?.id!!, Address::class.java)).isEmpty
        assertThat(entityDao.findAll(Role::class.java))
            .extracting("name")
            .containsExactlyInAnyOrder(*roles)
        assertThat(entityDao.findAll(Authority::class.java)).isEmpty()
    }
}
