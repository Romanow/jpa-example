package ru.romanow.jpa.service

import ru.romanow.jpa.model.PersonModifyRequest
import ru.romanow.jpa.model.PersonResponse

interface PersonService {
    fun findAll(): List<PersonResponse>
    fun findById(personId: Int): PersonResponse
    fun create(request: PersonModifyRequest): Int
    fun update(personId: Int, request: PersonModifyRequest): PersonResponse
    fun fullUpdate(personId: Int, request: PersonModifyRequest): PersonResponse
    fun delete(personId: Int)
    fun findByAddressId(addressId: Int): List<PersonResponse>
}
