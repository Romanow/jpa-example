package ru.romanow.jpa.web.utils;

import org.jetbrains.annotations.NotNull;
import ru.romanow.jpa.domain.Address;
import ru.romanow.jpa.domain.Authority;
import ru.romanow.jpa.domain.Person;
import ru.romanow.jpa.domain.Role;
import ru.romanow.jpa.model.AddressInfo;
import ru.romanow.jpa.model.AuthorityInfo;
import ru.romanow.jpa.model.PersonModifyRequest;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextInt;

public final class EntityBuilder {
    public static final int ROLE_COUNT = 2;
    public static final int AUTHORITY_COUNT = 3;

    @NotNull
    public static PersonModifyRequest buildPersonModifyRequest(int roleCount, int authorityCount) {
        return new PersonModifyRequest()
                .setFirstName(randomAlphabetic(8))
                .setMiddleName(randomAlphabetic(8))
                .setLastName(randomAlphabetic(8))
                .setAge(nextInt(18, 65))
                .setAddress(buildAddressInfo())
                .setRoles(range(0, roleCount)
                        .mapToObj(i -> randomAlphabetic(4))
                        .collect(toSet()))
                .setAuthorities(range(0, authorityCount)
                        .mapToObj(i -> new AuthorityInfo().setName(randomAlphabetic(4)).setPriority(nextInt(1, 10)))
                        .collect(toSet()));
    }

    @NotNull
    public static AddressInfo buildAddressInfo() {
        return new AddressInfo()
                .setCountry("Russia")
                .setCity("Moscow")
                .setStreet(randomAlphabetic(8))
                .setAddress(randomAlphabetic(8));
    }

    @NotNull
    public static Person buildPerson(int roleCount, int authorityCount) {
        return new Person()
                .setFirstName(randomAlphabetic(8))
                .setMiddleName(randomAlphabetic(8))
                .setLastName(randomAlphabetic(8))
                .setAge(nextInt(18, 65))
                .setAddress(buildAddress())
                .setRoles(range(0, roleCount)
                        .mapToObj(i -> new Role().setName(randomAlphabetic(4).toUpperCase()))
                        .collect(toSet()))
                .setAuthorities(range(0, authorityCount)
                        .mapToObj(i -> new Authority().setName(randomAlphabetic(4)).setPriority(nextInt(1, 10)))
                        .collect(toSet()));
    }

    @NotNull
    public static Address buildAddress() {
        return new Address()
                .setCountry("Russia")
                .setCity("Moscow")
                .setStreet(randomAlphabetic(8))
                .setAddress(randomAlphabetic(8));
    }
}
