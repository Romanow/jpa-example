package ru.romanow.jpa.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.romanow.jpa.domain.Person
import java.util.*

interface PersonRepository : JpaRepository<Person, Int> {
    @EntityGraph(attributePaths = ["address", "roles", "authorities"])
    override fun findById(id: Int): Optional<Person?>

    @EntityGraph(attributePaths = ["address", "roles", "authorities"])
    @Query("select p from Person p")
    fun findAllUsingGraph(): List<Person>

    @Query("select p from Person p join fetch p.address")
    fun findPersonWithAddress(): List<Person>

    @Query("select p from Person p where p.addressId = :addressId")
    fun findByAddressId(@Param("addressId") addressId: Int): List<Person>
}
