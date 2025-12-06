package ru.romanow.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import ru.romanow.jpa.config.DatabaseTestConfiguration
import ru.romanow.jpa.utils.AUTHORITY_COUNT
import ru.romanow.jpa.utils.ROLE_COUNT
import ru.romanow.jpa.utils.buildPerson

@DataJpaTest
@Import(DatabaseTestConfiguration::class)
internal class PersonRepositoryTest {

    @Autowired
    private lateinit var personRepository: PersonRepository

    @Autowired
    private lateinit var authorityRepository: AuthorityRepository

    @Test
    fun `when create new Person then save Authorities too`() {
        val person = buildPerson(ROLE_COUNT, AUTHORITY_COUNT)
        val authorities = person.authorities.map { it.name }.toTypedArray()
        val saved = personRepository.save(person)

        assertThat(saved).isNotNull()
        assertThat(saved.authorities).hasSize(3)
        assertThat(saved.authorities)
            .extracting("name")
            .containsExactlyInAnyOrder(*authorities)
    }

    @Test
    fun `when delete Person then delete Authorities too`() {
        val person = personRepository.save(buildPerson(ROLE_COUNT, AUTHORITY_COUNT))
        assertThat(authorityRepository.count()).isNotZero()

        personRepository.delete(person)
        personRepository.flush()

        assertThat(authorityRepository.count()).isZero()
    }
}
