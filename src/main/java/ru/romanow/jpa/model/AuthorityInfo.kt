package ru.romanow.jpa.model

data class AuthorityInfo(
    override var id: Int? = null,
    var name: String? = null,
    var priority: Int? = null
) : IdentifiableModel
