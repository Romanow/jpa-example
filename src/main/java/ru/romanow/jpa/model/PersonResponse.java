package ru.romanow.jpa.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;

@Data
@Accessors(chain = true)
public class PersonResponse {
    private Integer id;
    private String firstName;
    private String middleName;
    private String lastName;
    private Integer age;
    private AddressInfo address;
    private Set<String> roles;
    private List<AuthorityInfo> authorities;
}
