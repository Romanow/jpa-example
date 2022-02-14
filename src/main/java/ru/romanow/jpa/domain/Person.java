package ru.romanow.jpa.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "person")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name", length = 80, nullable = false)
    private String firstName;

    @Column(name = "middle_name", length = 80)
    private String middleName;

    @Column(name = "last_name", length = 80, nullable = false)
    private String lastName;

    @Column(name = "age")
    private Integer age;

    @Column(name = "address_id", updatable = false, insertable = false)
    private Integer addressId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    @JoinColumn(name = "address_id", foreignKey = @ForeignKey(name = "fk_person_address_id"))
    private Address address;

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST })
    @JoinTable(
            name = "person_roles",
            joinColumns = @JoinColumn(name = "person_id", foreignKey = @ForeignKey(name = "fk_person_roles_person_id")),
            inverseJoinColumns = @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_person_roles_role_id"))
    )
    private Set<Role> roles;

    @OneToMany(cascade = { CascadeType.PERSIST })
    @JoinColumn(name = "person_id", foreignKey = @ForeignKey(name = "fk_authority_person_id"))
    private List<Authority> authorities;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(firstName, person.firstName) && Objects.equals(middleName, person.middleName) && Objects.equals(lastName, person.lastName) && Objects.equals(age, person.age);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, middleName, lastName, age);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("firstName", firstName)
                .append("middleName", middleName)
                .append("lastName", lastName)
                .append("age", age)
                .toString();
    }
}
