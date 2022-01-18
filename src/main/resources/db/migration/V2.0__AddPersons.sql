-- V2.0 add persons
INSERT INTO address (id, city, country, street, address)
VALUES (1, 'Москва', 'Россия', 'Молостовых', '8 корпус 4');

INSERT INTO person (first_name, middle_name, last_name, age, address_id)
VALUES ('Алексей', 'Сергеевич', 'Романов', 32, 1);

INSERT INTO person (first_name, middle_name, last_name, age, address_id)
VALUES ('Екатерина', 'Валерьевна', 'Романова', 31, 1);