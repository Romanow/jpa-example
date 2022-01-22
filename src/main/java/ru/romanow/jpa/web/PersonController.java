package ru.romanow.jpa.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.romanow.jpa.model.PersonResponse;
import ru.romanow.jpa.service.PersonService;

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

    @GetMapping(value = "/address/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PersonResponse> personByAddressId(@PathVariable Integer id) {
        return personService.findByAddressId(id);
    }
}
