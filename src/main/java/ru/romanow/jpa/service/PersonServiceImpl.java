package ru.romanow.jpa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.romanow.jpa.mapper.PersonMapper;
import ru.romanow.jpa.model.PersonResponse;
import ru.romanow.jpa.repository.PersonRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl
        implements PersonService {
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    @Override
    public List<PersonResponse> findAll() {
        return personRepository.findAllUsingGraph()
                .stream()
                .map(personMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonResponse> findByAddressId(int id) {
        return personRepository.findWithAddress(id)
                .stream()
                .map(personMapper::toModel)
                .collect(Collectors.toList());
    }
}
