package ru.romanow.jpa.web.dao;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

@Repository
public class EntityDaoImpl
        implements EntityDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public <T> Optional<T> findById(@NotNull Integer id, Class<T> cls) {
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<T> query = criteriaBuilder.createQuery(cls);
        final Root<T> root = query.from(cls);
        query.select(root).where(criteriaBuilder.equal(root.get("id"), id));
        return entityManager
                .createQuery(query)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public <T> List<T> findAll(Class<T> cls) {
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<T> query = criteriaBuilder.createQuery(cls);
        final Root<T> root = query.from(cls);
        return entityManager
                .createQuery(query.select(root))
                .getResultList();
    }
}
