package ru.romanow.jpa.dao

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class EntityDaoImpl(@PersistenceContext private val entityManager: EntityManager) : EntityDao {

    override fun <T> findById(id: Int, cls: Class<T>): Optional<T> {
        val criteriaBuilder = entityManager.criteriaBuilder
        val query = criteriaBuilder.createQuery(cls)
        val root = query.from(cls)
        query.select(root).where(criteriaBuilder.equal(root.get<Any>("id"), id))
        return entityManager
            .createQuery(query)
            .setMaxResults(1)
            .resultList
            .stream()
            .findFirst()
    }

    override fun <T> findAll(cls: Class<T>): List<T> {
        val criteriaBuilder = entityManager.criteriaBuilder
        val query = criteriaBuilder.createQuery(cls)
        val root = query.from(cls)
        return entityManager
            .createQuery(query.select(root))
            .resultList
    }
}
