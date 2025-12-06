package ru.romanow.jpa.domain

import jakarta.persistence.*

@Entity
@Table(name = "authority")
data class Authority(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(name = "name", length = 80, nullable = false)
    var name: String? = null,

    @Column(name = "priority", nullable = false)
    var priority: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    val person: Person? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Authority) return false

        if (id != other.id) return false
        if (name != other.name) return false
        if (priority != other.priority) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (priority ?: 0)
        return result
    }

    override fun toString(): String {
        return "Authority(id=$id, name=$name, priority=$priority)"
    }
}
