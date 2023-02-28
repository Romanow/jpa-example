package ru.romanow.jpa.mapper;

import org.mapstruct.*;
import ru.romanow.jpa.domain.Address;
import ru.romanow.jpa.mapper.config.MapperConfiguration;
import ru.romanow.jpa.mapper.utils.FullUpdate;
import ru.romanow.jpa.mapper.utils.ReferenceMapper;
import ru.romanow.jpa.model.AddressInfo;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(config = MapperConfiguration.class, uses = ReferenceMapper.class)
public interface AddressMapper {
    AddressInfo toModel(Address address);

    @Mapping(target = "id", ignore = true)
    Address toEntity(AddressInfo address);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void update(AddressInfo request, @MappingTarget Address person);

    @FullUpdate
    @InheritConfiguration(name = "toEntity")
    void fullUpdate(AddressInfo request, @MappingTarget Address person);
}
