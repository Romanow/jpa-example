package ru.romanow.jpa.service;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.romanow.jpa.domain.Address;
import ru.romanow.jpa.domain.Authority;
import ru.romanow.jpa.domain.Person;
import ru.romanow.jpa.domain.Role;
import ru.romanow.jpa.mapper.AddressMapper;
import ru.romanow.jpa.mapper.AuthorityMapper;
import ru.romanow.jpa.mapper.PersonMapper;
import ru.romanow.jpa.model.PersonModifyRequest;
import ru.romanow.jpa.model.PersonResponse;
import ru.romanow.jpa.repository.PersonRepository;
import ru.romanow.jpa.repository.RoleRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Set;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl
        implements PersonService {
    private final PersonRepository personRepository;
    private final RoleRepository roleRepository;
    private final PersonMapper personMapper;
    private final AddressMapper addressMapper;
    private final AuthorityMapper authorityMapper;

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
        final Address address = new Address()
                .setCountry(request.getAddress().getCountry())
                .setCity(request.getAddress().getCity())
                .setStreet(request.getAddress().getStreet())
                .setAddress(request.getAddress().getAddress());

        final Set<Role> roles = request.getRoles()
                .stream()
                .map(r -> roleRepository
                        .findByName(r)
                        .orElse(new Role().setName(r)))
                .collect(toSet());

        final Set<Authority> authorities = request.getAuthorities()
                .stream()
                .map(a -> new Authority()
                        .setName(a.getName())
                        .setPriority(a.getPriority()))
                .collect(toSet());

        final Person person = new Person()
                .setFirstName(request.getFirstName())
                .setMiddleName(request.getMiddleName())
                .setLastName(request.getLastName())
                .setAge(request.getAge())
                .setAddress(address)
                .setRoles(roles)
                .setAuthorities(authorities);

        return personRepository.save(person).getId();
    }

    @NotNull
    @Override
    @Transactional
    public PersonResponse update(int personId, @NotNull PersonModifyRequest request) {
        final Person person = personRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Person for id '" + personId + "' not found"));

        personMapper.update(request, person);
        addressMapper.update(request.getAddress(), person.getAddress());
        if (request.getRoles() != null) {
            var existingRoles = person
                    .getRoles()
                    .stream()
                    .collect(toMap(Role::getName, identity()));
            for (var roleName : request.getRoles()) {
                if (!existingRoles.containsKey(roleName)) {
                    var role = roleRepository
                            .findByName(roleName)
                            .orElse(new Role().setName(roleName));
                    person.getRoles().add(role);
                }
            }
        }

        if (request.getAuthorities() != null) {
            var existingAuthorities = person
                    .getAuthorities()
                    .stream()
                    .collect(toMap(Authority::getId, identity()));
            for (var authority : request.getAuthorities()) {
                var authorityId = authority.getId();
                if (authorityId != null && existingAuthorities.containsKey(authorityId)) {
                    var existingAuthority = existingAuthorities.get(authorityId);
                    authorityMapper.update(authority, existingAuthority);
                } else {
                    var newAuthority = new Authority();
                    authorityMapper.fullUpdate(authority, newAuthority);
                    person.getAuthorities().add(newAuthority);
                }
            }
        }

        return personMapper.toModel(person);
    }

    @NotNull
    @Override
    @Transactional
    public PersonResponse fullUpdate(int personId, @NotNull PersonModifyRequest request) {
        final var person = personRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Person for id '" + personId + "' not found"));
        final var newPerson = personMapper.toModel(request);
        final var fullUpdatePerson = personMapper.fullUpdate(newPerson, person);
        return personMapper.toModel(fullUpdatePerson);
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