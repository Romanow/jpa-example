package ru.romanow.jpa.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.romanow.jpa.domain.Person
import ru.romanow.jpa.mapper.PersonFullUpdateMapper
import ru.romanow.jpa.mapper.PersonMapper
import ru.romanow.jpa.model.PersonModifyRequest
import ru.romanow.jpa.model.PersonResponse
import ru.romanow.jpa.repository.PersonRepository

@Service
class PersonServiceImpl(
    private val personRepository: PersonRepository,
    private val personMapper: PersonMapper,
    private val updateMapper: PersonFullUpdateMapper
) : PersonService {

    @Transactional(readOnly = true)
    override fun findById(personId: Int): PersonResponse =
        personRepository.findById(personId)
            .map { personMapper.toModel(it!!) }
            .orElseThrow { EntityNotFoundException("Person for id '$personId' not found") }


    @Transactional
    override fun create(request: PersonModifyRequest): Int {
        val person: Person = personMapper.toEntity(request)
        return personRepository.save(person).id!!
    }

    @Transactional
    override fun update(personId: Int, request: PersonModifyRequest): PersonResponse =
        personRepository.findById(personId)
            .map {
                personMapper.update(request, it!!)
                return@map personMapper.toModel(it)
            }
            .orElseThrow { EntityNotFoundException("Person for id '$personId' not found") }

    @Transactional
    override fun fullUpdate(personId: Int, request: PersonModifyRequest): PersonResponse =
        personRepository
            .findById(personId)
            .map {
                updateMapper.fullUpdate(request, it!!)
                return@map personMapper.toModel(it)
            }
            .orElseThrow { EntityNotFoundException("Person for id '$personId' not found") }

    @Transactional
    override fun delete(personId: Int) {
        personRepository.deleteById(personId)
    }

    @Transactional(readOnly = true)
    override fun findAll() =
        personRepository.findAllUsingGraph().map { personMapper.toModel(it) }

    @Transactional(readOnly = true)
    override fun findByAddressId(addressId: Int) =
        personRepository.findByAddressId(addressId).map { personMapper.toModel(it) }
}
