package ru.romanow.jpa.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.romanow.jpa.domain.Person;

import java.util.List;
import java.util.Optional;

public interface PersonRepository
        extends JpaRepository<Person, Integer> {

    @NotNull
    @EntityGraph(attributePaths = {"address", "roles", "authorities"})
    Optional<Person> findById(@NotNull Integer id);

    @EntityGraph(attributePaths = {"address", "roles", "authorities"})
    @Query("select p from Person p")
    List<Person> findAllUsingGraph();

    @Query("select p from Person p join fetch p.address")
    List<Person> findPersonWithAddress();

    @Query("select p from Person p where p.addressId = :addressId")
    List<Person> findByAddressId(@Param("addressId") Integer addressId);
}
