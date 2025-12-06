-- V1.0 init person and address table
CREATE TABLE address
(
    id      SERIAL PRIMARY KEY,
    city    VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL,
    street  VARCHAR(255),
    address VARCHAR(255) NOT NULL
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

CREATE INDEX idx_person_address_id ON person (address_id);

CREATE TABLE role
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(80) NOT NULL
);

CREATE UNIQUE INDEX idx_role_name ON role (id);

CREATE TABLE person_roles
(
    person_id INT
        CONSTRAINT fk_person_roles_person_id REFERENCES person (id),
    role_id   INT
        CONSTRAINT fk_person_roles_role_id REFERENCES role (id)
);

CREATE INDEX idx_person_roles_person_id_and_role_id ON person_roles (person_id, role_id);

CREATE TABLE authority
(
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(80) NOT NULL,
    priority  INT         NOT NULL,
    person_id INT
        CONSTRAINT fk_authority_person_id REFERENCES person (id)
);

CREATE INDEX idx_authority_person_id ON authority (person_id);
