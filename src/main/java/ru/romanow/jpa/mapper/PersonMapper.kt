package ru.romanow.jpa.mapper

import org.mapstruct.*
import org.mapstruct.NullValuePropertyMappingStrategy.IGNORE
import ru.romanow.jpa.domain.Person
import ru.romanow.jpa.mapper.config.MapperConfiguration
import ru.romanow.jpa.mapper.utils.ReferenceMapper
import ru.romanow.jpa.model.PersonModifyRequest
import ru.romanow.jpa.model.PersonResponse

@Mapper(
    config = MapperConfiguration::class,
    collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    uses = [AddressMapper::class, AuthorityMapper::class, RoleMapper::class, ReferenceMapper::class]
)
interface PersonMapper {
    fun toModel(person: Person): PersonResponse

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addressId", ignore = true)
    fun toEntity(response: PersonModifyRequest): Person

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    fun update(request: PersonModifyRequest, @MappingTarget person: Person)
}
