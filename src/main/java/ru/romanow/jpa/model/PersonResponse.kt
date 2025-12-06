package ru.romanow.jpa.model

data class PersonResponse(
    override var id: Int? = null,
    var firstName: String? = null,
    var middleName: String? = null,
    var lastName: String? = null,
    var age: Int? = null,
    var address: AddressInfo? = null,
    var roles: Set<String>? = null,
    var authorities: Set<AuthorityInfo>? = null
) : IdentifiableModel
