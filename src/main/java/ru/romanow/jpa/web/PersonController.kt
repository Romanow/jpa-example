package ru.romanow.jpa.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.romanow.jpa.model.PersonModifyRequest;
import ru.romanow.jpa.model.PersonResponse;
import ru.romanow.jpa.service.PersonService;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/persons")
public class PersonController {
    private final PersonService personService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PersonResponse> persons() {
        return personService.findAll();
    }

    @GetMapping(value = "/{personId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PersonResponse personById(@PathVariable Integer personId) {
        return personService.findById(personId);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> create(@RequestBody PersonModifyRequest request) {
        int id = personService.create(request);
        final URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(uri).build();
    }

    @PatchMapping(value = "/{personId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PersonResponse update(@PathVariable Integer personId, @RequestBody PersonModifyRequest request) {
        return personService.update(personId, request);
    }

    @PutMapping(value = "/{personId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PersonResponse fullUpdate(@PathVariable Integer personId, @RequestBody PersonModifyRequest request) {
        return personService.fullUpdate(personId, request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{personId}")
    public void delete(@PathVariable Integer personId) {
        personService.delete(personId);
    }

    @GetMapping(value = "/address/{addressId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PersonResponse> personByAddressId(@PathVariable Integer addressId) {
        return personService.findByAddressId(addressId);
    }
}
