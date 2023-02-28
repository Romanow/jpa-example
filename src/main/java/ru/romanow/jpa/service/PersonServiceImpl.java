package ru.romanow.jpa.service;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.romanow.jpa.domain.Person;
import ru.romanow.jpa.mapper.PersonFullUpdateMapper;
import ru.romanow.jpa.mapper.PersonMapper;
import ru.romanow.jpa.model.PersonModifyRequest;
import ru.romanow.jpa.model.PersonResponse;
import ru.romanow.jpa.repository.PersonRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl
        implements PersonService {
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final PersonFullUpdateMapper updateMapper;

    @NotNull
    @Override
    public PersonResponse findById(int personId) {
        return personRepository.findById(personId)
                .map(personMapper::toModel)
                .orElseThrow(() -> new EntityNotFoundException("Person for id '" + personId + "' not found"));
    }

    @Override
    @Transactional
    public int create(@NotNull PersonModifyRequest request) {
        final Person person = personMapper.toEntity(request);
        return personRepository.save(person).getId();
    }

    @NotNull
    @Override
    @Transactional
    public PersonResponse update(int personId, @NotNull PersonModifyRequest request) {
        return personRepository.findById(personId)
                .map(person -> {
                    personMapper.update(request, person);
                    return person;
                })
                .map(personMapper::toModel)
                .orElseThrow(() -> new EntityNotFoundException("Person for id '" + personId + "' not found"));
    }

    @NotNull
    @Override
    @Transactional
    public PersonResponse fullUpdate(int personId, @NotNull PersonModifyRequest request) {
        return personRepository
                .findById(personId)
                .map(person -> {
                    updateMapper.fullUpdate(request, person);
                    return person;
                })
                .map(personMapper::toModel)
                .orElseThrow(() -> new EntityNotFoundException("Person for id '" + personId + "' not found"));
    }

    @Override
    @Transactional
    public void delete(int personId) {
        personRepository.deleteById(personId);
    }

    @NotNull
    @Override
    public List<PersonResponse> findAll() {
        return personRepository.findAllUsingGraph()
                .stream()
                .map(personMapper::toModel)
                .collect(toList());
    }

    @NotNull
    @Override
    @Transactional(readOnly = true)
    public List<PersonResponse> findByAddressId(int addressId) {
        return personRepository.findByAddressId(addressId)
                .stream()
                .map(personMapper::toModel)
                .collect(toList());
    }
}
