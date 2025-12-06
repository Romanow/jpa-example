package ru.romanow.jpa.mapper.utils

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.mapstruct.ObjectFactory
import org.mapstruct.TargetType
import org.springframework.stereotype.Component
import ru.romanow.jpa.model.IdentifiableModel

@Component
class ReferenceMapper(@PersistenceContext private val entityManager: EntityManager) {

    @ObjectFactory
    fun <T> resolve(model: IdentifiableModel, @TargetType type: Class<T>): T {
        if (model.id != null) {
            val entity: T = entityManager.getReference(type, model.id)
            return entity ?: newInstance(type)
        } else {
            return newInstance(type)
        }
    }

    private fun <T> newInstance(type: Class<T>): T {
        return type.getDeclaredConstructor().newInstance()
    }
}
