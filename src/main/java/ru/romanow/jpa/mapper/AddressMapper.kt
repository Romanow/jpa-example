package ru.romanow.jpa.mapper

import org.mapstruct.*
import org.mapstruct.NullValuePropertyMappingStrategy.IGNORE
import ru.romanow.jpa.domain.Address
import ru.romanow.jpa.mapper.config.MapperConfiguration
import ru.romanow.jpa.mapper.utils.FullUpdate
import ru.romanow.jpa.mapper.utils.ReferenceMapper
import ru.romanow.jpa.model.AddressInfo

@Mapper(config = MapperConfiguration::class, uses = [ReferenceMapper::class])
interface AddressMapper {
    fun toModel(address: Address): AddressInfo

    @Mapping(target = "id", ignore = true)
    fun toEntity(address: AddressInfo): Address

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    fun update(request: AddressInfo, @MappingTarget address: Address)

    @FullUpdate
    @InheritConfiguration(name = "toEntity")
    fun fullUpdate(request: AddressInfo, @MappingTarget address: Address)
}
