package ru.romanow.jpa.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "street")
    private String street;

    @Column(name = "address", nullable = false)
    private String address;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address other = (Address) o;
        return Objects.equals(city, other.city) && Objects.equals(country, other.country) && Objects.equals(street, other.street) && Objects.equals(address, other.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, country, street, address);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("city", city)
                .append("country", country)
                .append("street", street)
                .append("address", address)
                .toString();
    }
}
