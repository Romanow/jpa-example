package ru.romanow.jpa.model;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;

@Data
@Accessors(chain = true)
public class AddressInfo {
    private String city;
    private String country;
    private String street;
    private String address;
}
