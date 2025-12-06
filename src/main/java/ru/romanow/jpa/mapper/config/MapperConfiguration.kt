package ru.romanow.jpa.mapper.config

import org.mapstruct.InjectionStrategy
import org.mapstruct.MapperConfig
import org.mapstruct.ReportingPolicy

@MapperConfig(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
interface MapperConfiguration
