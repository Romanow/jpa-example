package ru.romanow.jpa.domain

import jakarta.persistence.*

@Entity
@Table(name = "address")
data class Address(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(name = "city", nullable = false)
    var city: String? = null,

    @Column(name = "country", nullable = false)
    var country: String? = null,

    @Column(name = "street")
    var street: String? = null,

    @Column(name = "address", nullable = false)
    var address: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Address) return false

        if (id != other.id) return false
        if (city != other.city) return false
        if (country != other.country) return false
        if (street != other.street) return false
        if (address != other.address) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + (city?.hashCode() ?: 0)
        result = 31 * result + (country?.hashCode() ?: 0)
        result = 31 * result + (street?.hashCode() ?: 0)
        result = 31 * result + (address?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Address(id=$id, city=$city, country=$country, street=$street, address=$address)"
    }
}
