package ru.romanow.jpa.service;

import org.jetbrains.annotations.NotNull;
import ru.romanow.jpa.model.PersonModifyRequest;
import ru.romanow.jpa.model.PersonResponse;

import java.util.List;

public interface PersonService {
    @NotNull
    List<PersonResponse> findAll();

    @NotNull
    PersonResponse findById(int personId);

    int create(@NotNull PersonModifyRequest request);

    @NotNull
    PersonResponse update(int personId, @NotNull PersonModifyRequest request);

    @NotNull
    PersonResponse fullUpdate(int personId, @NotNull PersonModifyRequest request);

    void delete(int personId);

    @NotNull
    List<PersonResponse> findByAddressId(int addressId);
}
