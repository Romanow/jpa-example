package ru.romanow.jpa.repository

import org.springframework.data.repository.CrudRepository
import ru.romanow.jpa.domain.Role
import java.util.*

interface RoleRepository : CrudRepository<Role, Int> {
    fun findByName(name: String): Optional<Role>
}
