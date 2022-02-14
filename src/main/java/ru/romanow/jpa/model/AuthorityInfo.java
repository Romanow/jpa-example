package ru.romanow.jpa.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AuthorityInfo {
    private Integer id;
    private String name;
    private Integer priority;
}
