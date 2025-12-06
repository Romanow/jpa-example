package ru.romanow.jpa.model

data class AddressInfo(
    override var id: Int? = null,
    var city: String? = null,
    var country: String? = null,
    var street: String? = null,
    var address: String? = null
) : IdentifiableModel
