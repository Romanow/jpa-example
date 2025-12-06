package ru.romanow.jpa.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AddressInfo
        implements IdentifiableModel {
    private Integer id;
    private String city;
    private String country;
    private String street;
    private String address;
}
