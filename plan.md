# План доклада

1. Что за настройка `spring.jpa.open-in-view=false` и почему появилась проблема?
2. Что такое MapStruct, для чего нужна эта библиотека.
3. Рассматриваем два подхода:
   * MapStruct как простой перекладчик примитивных типов, вся сложная логика остается в сервисе.
   * MapStruct выполняет всю работу по преобразованию DTO <-> Entity.
4. Пример с CRUD операциями над сущностью `Person` в связке с подчиненными сущностями: `Address` (1:1), `Authorities` (
   1:N), `Roles` (N:N).
   * GET by ID: Entity -> DTO.
   * POST (Create new Person): DTO -> create new Entity.
   * PATCH (Partial Update): DTO -> partial update Entity.
   * PUT (Full Update): DTO -> full update Entity.
5. Обсуждаем вопросы тестирования.
6. Выводы (~~пытаемся сформулировать правила работы с MapStruct~~).

## Редактирование

* PATCH на удаление разделить на update / delete.

## Примеры

Сущности `Person` <--1:N-- `Address`, `Person` <--N:N--> `Roles`.

Person <--1:N--> Address, Address подчиненная сущность, т.е. без Person или City быть не может.

1. GET Person + Address, вернуть Person и все Address, Roles;
2. POST Person + Address: создание нового Address и привязать к Person;
3. PATCH Person + Address: обновление Address либо по addressId, либо создаем новые, вне запроса записи не изменяем;
4. PUT Person + Address: полная перезапись массива Address, лишние удаляем;
5. DELETE Person: удаление Person и Address.

### Особенности реализации

1. `@EntityGraph` по-умолчанию `type = EntityGraphType.FETCH`, это значит что описанные сущности Hibernate поднимает как
   EAGER, а все остальные считает как LAZY (даже если в `@Entity` они описаны как EAGER). `EntityGraphType.LOAD` берет
   берет из описания `@Entity`.
2. `@EntityGraph` игнорирует `@Fetch(FetchMode.SUBSELECT)` и все поднимает через JOIN.
3. `@JoinColumn` на `@OneToMany`/`@ManyToOne` определяет главную сущность. Без этого не будет работать связывание
   объектов с родительской сущностью при добавлении в массив `@OneToMany`, т.е. будет:

    ```
    Hibernate:
        insert into authority (id, name, person_id, priority) values (null, ?, ?, ?)
    2022-02-14 15:33:16.790 TRACE 79689 --- [           main] o.h.type.descriptor.sql.BasicBinder      : binding parameter [1] as [VARCHAR] - [IItm]
    2022-02-14 15:33:16.790 TRACE 79689 --- [           main] o.h.type.descriptor.sql.BasicBinder      : binding parameter [2] as [INTEGER] - [null]
    2022-02-14 15:33:16.790 TRACE 79689 --- [           main] o.h.type.descriptor.sql.BasicBinder      : binding parameter [3] as [INTEGER] - [2]
    ...
    Hibernate: 
        updateauthority set person_id=? where id=?
    2022-02-14 15:33:16.828 TRACE 79689 --- [           main] o.h.type.descriptor.sql.BasicBinder      : binding parameter [1] as [INTEGER] - [1]
    2022-02-14 15:33:16.828 TRACE 79689 --- [           main] o.h.type.descriptor.sql.BasicBinder      : binding parameter [2] as [INTEGER] - [1]
    ```
5. Для удаления старых записей при обновлении объекта `@OneToMany` нужно сделать:

    ```jshelllanguage
    person.getAuthorities().clear();
    person.getAuthorities().addAll(newAuthorities);
    ```