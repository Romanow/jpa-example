package ru.romanow.jpa.utils

import ru.romanow.jpa.domain.Address
import ru.romanow.jpa.domain.Authority
import ru.romanow.jpa.domain.Person
import ru.romanow.jpa.domain.Role
import ru.romanow.jpa.model.AddressInfo
import ru.romanow.jpa.model.AuthorityInfo
import ru.romanow.jpa.model.PersonModifyRequest
import org.apache.commons.lang3.RandomStringUtils.insecure as string
import org.apache.commons.lang3.RandomUtils.insecure as random

const val ROLE_COUNT: Int = 2
const val AUTHORITY_COUNT: Int = 3

fun buildPersonModifyRequest(roleCount: Int, authorityCount: Int) =
    PersonModifyRequest(
        firstName = string().nextAlphabetic(8),
        middleName = string().nextAlphabetic(8),
        lastName = string().nextAlphabetic(8),
        age = random().randomInt(18, 65),
        address = buildAddressInfo(),
        roles = MutableList(roleCount) { string().nextAlphabetic(4).uppercase() }.toSet(),
        authorities = MutableList(authorityCount) {
            AuthorityInfo(name = string().nextAlphabetic(4), priority = random().randomInt(1, 10))
        }.toSet()
    )

fun buildPerson(roleCount: Int, authorityCount: Int) =
    Person(
        firstName = string().nextAlphabetic(8),
        middleName = string().nextAlphabetic(8),
        lastName = string().nextAlphabetic(8),
        age = random().randomInt(18, 65),
        address = buildAddress(),
        roles = MutableList(roleCount) { Role(name = string().nextAlphabetic(4).uppercase()) }.toMutableSet(),
        authorities = MutableList(authorityCount) {
            Authority(name = string().nextAlphabetic(4), priority = random().randomInt(1, 10))
        }.toMutableSet()
    )

fun buildAddressInfo() = AddressInfo(
    country = "Russia",
    city = "Moscow",
    street = string().nextAlphabetic(8),
    address = string().nextAlphabetic(8)
)

fun buildAddress() = Address(
    country = "Russia",
    city = "Moscow",
    street = string().nextAlphabetic(8),
    address = string().nextAlphabetic(8)
)
