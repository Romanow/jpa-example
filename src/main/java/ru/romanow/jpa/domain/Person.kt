package ru.romanow.jpa.domain

import jakarta.persistence.*

@Entity
@Table(name = "person")
data class Person(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(name = "first_name", length = 80, nullable = false)
    var firstName: String? = null,

    @Column(name = "middle_name", length = 80)
    var middleName: String? = null,

    @Column(name = "last_name", length = 80, nullable = false)
    var lastName: String? = null,

    @Column(name = "age")
    var age: Int? = null,

    @Column(name = "address_id", updatable = false, insertable = false)
    var addressId: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.REMOVE])
    @JoinColumn(name = "address_id", foreignKey = ForeignKey(name = "fk_person_address_id"))
    var address: Address? = null,

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
    @JoinTable(
        name = "person_roles",
        joinColumns = [JoinColumn(name = "person_id", foreignKey = ForeignKey(name = "fk_person_roles_person_id"))],
        inverseJoinColumns = [JoinColumn(name = "role_id", foreignKey = ForeignKey(name = "fk_person_roles_role_id"))]
    )
    var roles: MutableSet<Role> = HashSet(),

    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.REMOVE], orphanRemoval = true)
    @JoinColumn(name = "person_id", foreignKey = ForeignKey(name = "fk_authority_person_id"))
    var authorities: MutableSet<Authority> = HashSet()
) {
    fun addRole(role: Role) {
        roles.add(role)
    }

    fun addAuthority(authority: Authority) {
        authorities.add(authority)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Person) return false

        if (id != other.id) return false
        if (firstName != other.firstName) return false
        if (middleName != other.middleName) return false
        if (lastName != other.lastName) return false
        if (age != other.age) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + (firstName?.hashCode() ?: 0)
        result = 31 * result + (middleName?.hashCode() ?: 0)
        result = 31 * result + (lastName?.hashCode() ?: 0)
        result = 31 * result + (age ?: 0)
        return result
    }

    override fun toString(): String {
        return "Person(age=$age, lastName=$lastName, middleName=$middleName, firstName=$firstName, id=$id)"
    }
}
