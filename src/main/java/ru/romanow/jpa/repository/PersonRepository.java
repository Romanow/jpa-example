package ru.romanow.jpa.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.romanow.jpa.domain.Person;

import java.util.List;
import java.util.Optional;

public interface PersonRepository
        extends JpaRepository<Person, Integer> {
    @Query("select p from Person p join fetch p.address")
    List<Person> findPersonAndAddress();

    @EntityGraph(attributePaths = "address")
    @Query("select p from Person p")
    List<Person> findAllUsingGraph();

    @Query("select p from Person p where p.addressId = :id")
    List<Person> findWithAddress(@Param("id") Integer id);
}
