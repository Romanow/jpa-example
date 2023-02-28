package ru.romanow.jpa.web.dao;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EntityDao {
    <T> Optional<T> findById(@NotNull Integer id, Class<T> cls);

    <T> List<T> findAll(Class<T> cls);
}
