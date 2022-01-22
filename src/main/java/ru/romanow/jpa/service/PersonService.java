package ru.romanow.jpa.service;

import ru.romanow.jpa.model.PersonResponse;

import java.util.List;

public interface PersonService {
    List<PersonResponse> findAll();

    List<PersonResponse> findByAddressId(int id);
}
