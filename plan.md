# План доклада

1. Что за настройка `spring.jpa.open-in-view=false` и почему появилась пробелма?
2. Зачем вообще нужны транзакции в коде?
    1. Что такое ACID.
    2. Как устроен @Transactional? Не работает при вызове метода того же класа и private методов.
    3. `@Transactional(readOnly = true)`.
    4. Что такое транзакционная целостность?
    5. Где правильно использовать транзации?
        1. Паттерн MVC, выделение бизнес-слоя `service`.
        2. Привет структуры пакетов, объяснение доменной области объекта.
3. Автогенерация DDL.
    1. Плюсы, минусы.
    2. Инструменты для миграции:
        1. Flyway;
        2. Liquibase.
    3. Валидация схемы и `@Entity` (`spring.jpa.hibernate.ddl-auto=validate`).
4. Типы связей между сущностями:
    1. `@OneToOne`
    2. `@OneToMany`
    3. `@ManyToOne`
    4. `@ManyToMany`
5. Как работать с LAZY–сущностями?
    1. Обрамляющая транзакция `@Transactional`.
    2. Выполнение внешних вызовов из сервиса, помеченного `@Transcational`:
        1. REST запрос;
        2. Отправка данных в очередь.
    3. `join fetch`.
    4. `@EntityGraph`.
6. Работа с MapStruct:
    1. Использование DTO для внешнего API.
    2. Что такое MapStruct, для чего нужна эта библиотека.
    3. При маппинге `@Entity` -> DTO не должно быть сложных преобразований, их лучше делать руками.
    4. Маппинг `DTO` -> `@Entity`
        1. Как работает `CascadeType`?
        2. Когда нужен `orphalRemove = true`
        3. Создание нового `@Entity`.
        4. Обновление существующего.
    5. Правила работы с Mapper.

## Примеры

Сущности `Person` <--1:N-- `Address`, `Person` <--N:N--> `Roles`.

Person <--1:N--> Address, Address подчиненная сущность, т.е. без Person или City быть не может.

1. GET Person + Address, вернуть Person и все Address, Roles;
2. POST Person + Address: создание нового Address и привязать к Person;
3. PATCH Person + Address: обновление Address либо по addressId, либо создаем новые, вне запроса записи не изменяем;
3. PUT Person + Address: полная перезапись массива Address, лишние удаляем;
4. DELETE Person: удаление Person и Address.