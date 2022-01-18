-- V1.0 init person and address table
CREATE TABLE address
(
    id      SERIAL PRIMARY KEY,
    city    VARCHAR(255),
    country VARCHAR(255),
    street  VARCHAR(255),
    address VARCHAR(255)
);

CREATE TABLE person
(
    id          SERIAL PRIMARY KEY,
    first_name  VARCHAR(80) NOT NULL,
    middle_name VARCHAR(80),
    last_name   VARCHAR(80) NOT NULL,
    age         INT,
    address_id  INT
        CONSTRAINT fk_person_address_id REFERENCES address (id)
);