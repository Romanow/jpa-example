package ru.romanow.jpa.mapper;

import org.mapstruct.MapperConfig;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.ReportingPolicy.ERROR;

@MapperConfig(componentModel = "spring", injectionStrategy = CONSTRUCTOR, unmappedTargetPolicy = ERROR)
public interface MapperConfiguration {
}
