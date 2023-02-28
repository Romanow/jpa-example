package ru.romanow.jpa.mapper.utils;

import lombok.SneakyThrows;
import org.mapstruct.ObjectFactory;
import org.mapstruct.TargetType;
import org.springframework.stereotype.Component;
import ru.romanow.jpa.model.IdentifiableModel;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Component
public class ReferenceMapper {

    @PersistenceContext
    private EntityManager entityManager;

    @ObjectFactory
    public <T> T resolve(IdentifiableModel model, @TargetType Class<T> type) {
        if (model.getId() != null) {
            final var entity = entityManager.getReference(type, model.getId());
            return entity != null ? entity : newInstance(type);
        } else {
            return newInstance(type);
        }
    }

    @SneakyThrows
    private <T> T newInstance(Class<T> type) {
        return type.getDeclaredConstructor().newInstance();
    }
}
