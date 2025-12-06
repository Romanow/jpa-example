package ru.romanow.jpa.domain

import jakarta.persistence.*

@Entity
@Table(name = "role")
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(name = "name", length = 80, nullable = false)
    var name: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Role) return false

        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Role(id=$id, name=$name)"
    }
}
