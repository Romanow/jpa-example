package ru.romanow.jpa.model;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;

@Data
@Accessors(chain = true)
public class PersonResponse {
    private String firstName;
    private String middleName;
    private String lastName;
    private Integer age;
    private AddressInfo address;
}
