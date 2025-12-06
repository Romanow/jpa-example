package ru.romanow.jpa.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.ObjectFactory
import org.springframework.beans.factory.annotation.Autowired
import ru.romanow.jpa.domain.Role
import ru.romanow.jpa.mapper.config.MapperConfiguration
import ru.romanow.jpa.repository.RoleRepository

@Mapper(config = MapperConfiguration::class)
abstract class RoleMapper {
    @Autowired
    private lateinit var roleRepository: RoleRepository

    fun toModel(role: Role): String? {
        return role.name
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    abstract fun toEntity(name: String?): Role?

    @ObjectFactory
    fun resolve(name: String): Role {
        return roleRepository.findByName(name).orElse(Role(name = name))
    }
}
