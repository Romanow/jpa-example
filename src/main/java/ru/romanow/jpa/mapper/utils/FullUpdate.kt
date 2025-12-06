package ru.romanow.jpa.mapper.utils

import org.mapstruct.Qualifier

@Qualifier
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.BINARY)
annotation class FullUpdate
