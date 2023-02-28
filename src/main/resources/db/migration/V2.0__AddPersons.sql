-- V2.0 add persons
INSERT INTO address (id, city, country, street, address)
VALUES (100, 'Москва', 'Россия', 'Молостовых', '8 корпус 4');

INSERT INTO person (id, first_name, middle_name, last_name, age, address_id)
VALUES (100, 'Алексей', 'Сергеевич', 'Романов', 32, 100);

INSERT INTO person (id, first_name, middle_name, last_name, age, address_id)
VALUES (101, 'Екатерина', 'Валерьевна', 'Романова', 31, 100);