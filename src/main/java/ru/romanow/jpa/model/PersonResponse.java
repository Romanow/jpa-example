package ru.romanow.jpa.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@Accessors(chain = true)
public class PersonResponse
        implements IdentifiableModel {
    private Integer id;
    private String firstName;
    private String middleName;
    private String lastName;
    private Integer age;
    private AddressInfo address;
    private Set<String> roles;
    private Set<AuthorityInfo> authorities;
}
