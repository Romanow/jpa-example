package ru.romanow.jpa.mapper

import org.mapstruct.*
import org.mapstruct.NullValuePropertyMappingStrategy.IGNORE
import ru.romanow.jpa.domain.Authority
import ru.romanow.jpa.mapper.config.MapperConfiguration
import ru.romanow.jpa.mapper.utils.FullUpdate
import ru.romanow.jpa.mapper.utils.ReferenceMapper
import ru.romanow.jpa.model.AuthorityInfo

@Mapper(config = MapperConfiguration::class, uses = [ReferenceMapper::class])
interface AuthorityMapper {
    fun toModel(grant: Authority): AuthorityInfo

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "person", ignore = true)
    fun toEntity(request: AuthorityInfo): Authority

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    fun update(request: AuthorityInfo, @MappingTarget authority: Authority)

    @FullUpdate
    @InheritConfiguration(name = "toEntity")
    fun fullUpdate(request: AuthorityInfo, @MappingTarget authority: Authority)
}
