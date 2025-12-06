package ru.romanow.jpa.web

import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import ru.romanow.jpa.model.PersonModifyRequest
import ru.romanow.jpa.service.PersonService

@RestController
@RequestMapping("/api/v1/persons")
class PersonController(private val personService: PersonService) {

    @GetMapping(produces = [APPLICATION_JSON_VALUE])
    fun persons() = personService.findAll()

    @GetMapping(value = ["/{personId}"], produces = [APPLICATION_JSON_VALUE])
    fun personById(@PathVariable personId: Int) = personService.findById(personId)

    @PostMapping(consumes = [APPLICATION_JSON_VALUE])
    fun create(@RequestBody request: PersonModifyRequest): ResponseEntity<Void> {
        val id = personService.create(request)
        val uri = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(id)
            .toUri()

        return ResponseEntity.created(uri).build()
    }

    @PatchMapping(value = ["/{personId}"], consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun update(@PathVariable personId: Int, @RequestBody request: PersonModifyRequest) =
        personService.update(personId, request)

    @PutMapping(value = ["/{personId}"], consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun fullUpdate(@PathVariable personId: Int, @RequestBody request: PersonModifyRequest) =
        personService.fullUpdate(personId, request)

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/{personId}")
    fun delete(@PathVariable personId: Int) {
        personService.delete(personId)
    }

    @GetMapping(value = ["/address/{addressId}"], produces = [APPLICATION_JSON_VALUE])
    fun personByAddressId(@PathVariable addressId: Int) = personService.findByAddressId(addressId)
}
