# JPA example

## Использование Hibernate + JPA

##### Настройка JPA spring.jpa.open-in-view=false

##### Использование автогенерации схем данных JPA в прод среде запрещено

Автогенерация DDL занимает много времени, т.к. Hibernate через метаинформацию вытягивает структуру БД и сравнивает ее с
описанием в `@Entity`.

Правильным и контролируемым подходом для работы со схемой базы данных являются скрипты миграции. Для Java есть два
основных инструмента:

* [Flyway](https://flywaydb.org/documentation/usage/plugins/springboot), интеграция со Spring
  Boot [Use a Higher-level Database Migration Tool](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto.data-initialization.migration-tool).
* [Liquibase](https://liquibase.org/get-started/quickstart), интеграция со Spring
  Boot [Using Liquibase with Spring Boot](https://docs.liquibase.com/tools-integrations/springboot/springboot.html).

Liquibase более мощный инструмент, например он умеет делать rollback изменений или импорт данных из CSV, но описание
миграций в нем реализуется через XML, что приносит некоторые неудобства.

Для production среды нужно _полностью_ выключить генерацию DDL.

```properties
spring.jpa.generate-ddl=false
spring.jpa.hibernate.ddl-auto=none
```

Для тестовых сред возможно использовать уровень `validate`, чтобы гарантировать консистентность схемы БД и
описания `@Entity`.

```properties
spring.jpa.generate-ddl=true
spring.jpa.hibernate.ddl-auto=validate
```

##### Применять тип загрузки FetchType.LAZY

Существуют 4 типа связей сущностей в Hibernate:

* `@OneToOne` (EAGER) – связь
* `@OneToMany` (LAZY)
* `@ManyToOne` (EAGER)
* `@ManyToMany` (LAZY)

##### В коде используем явное управление транзакциями через `@Transactional`

### Пояснения и комментарии

#### Использование MapStruct

##### Маппинг Entity -> DTO

##### Маппинг DTO -> Entity

#### Выполнение внешних вызовов из сервиса, помеченного `@Transcational`

##### REST запрос

##### Отправка данных через очередь

## Примеры

```java
/**
 * Register OpenEntityManagerInViewInterceptor. Binds a JPA EntityManager to the
 * thread for the entire processing of the request.
 */
private Boolean openInView;
```

Если выключаем `spring.jpa.open-in-view=false`, тогда при запросе `GET http://localhost:8080/` получаем
LazyInitializationException.

```
Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is org.hibernate.LazyInitializationException: could not initialize proxy [ru.romanow.jpa.domain.Address#1] - no Session] with root cause

org.hibernate.LazyInitializationException: could not initialize proxy [ru.romanow.jpa.domain.Address#1] - no Session
	at org.hibernate.proxy.AbstractLazyInitializer.initialize(AbstractLazyInitializer.java:170) ~[hibernate-core-5.4.32.Final.jar:5.4.32.Final]
	at org.hibernate.proxy.AbstractLazyInitializer.getImplementation(AbstractLazyInitializer.java:310) ~[hibernate-core-5.4.32.Final.jar:5.4.32.Final]
	at org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor.intercept(ByteBuddyInterceptor.java:45) ~[hibernate-core-5.4.32.Final.jar:5.4.32.Final]
	at org.hibernate.proxy.ProxyConfiguration$InterceptorDispatcher.intercept(ProxyConfiguration.java:95) ~[hibernate-core-5.4.32.Final.jar:5.4.32.Final]
	at ru.romanow.jpa.domain.Address$HibernateProxy$zoO4cARL.getCity(Unknown Source) ~[classes/:na]
	at ru.romanow.jpa.mapper.AddressMapperImpl.toModel(AddressMapperImpl.java:24) ~[classes/:na]
	at ru.romanow.jpa.mapper.PersonMapperImpl.toModel(PersonMapperImpl.java:32) ~[classes/:na]
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:195) ~[na:na]
	at java.base/java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1654) ~[na:na]
    ...
```

### Способы исправления

##### Использование `@Transactional` в сервисном слое

Если метод в сервисе пометить аннтоацией `@Transactional`, тогда подзапросы будут выполняться в рамках сессии:

```java
@Override
@Transactional(readOnly = true)
public List<PersonResponse> findAll(){
    return personRepository.findAll()
        .stream()
        .map(personMapper::toModel)
        .collect(Collectors.toList());
}
```

При этом сначала будет поднята сущность Person, а поле address будет HibernateProxy, который при первом обращении к
сущности выполнит дополнительный запрос к базе данных и поднимет Address по ID.

![Hibernate Interceptor](images/hibernate_interceptor.png)

При LAZY инициализации сущности, по ссылке на объект хранится Hibernate Proxy, который реализован с помощью библиотеки
ByteBuddy. При обращении к методу `person.getAddress()` срабатывает method
interceptor `$$_hibernate_interceptor: ByteBuddyInterceptor`, который содержит всю необходимую информацию для выполнения
запроса к БД. После первого запроса внутри Hibernate Proxy заполняется поле `target` и уже все последующие запросы к
сущности делегируются к этому полю.

##### Использование `@Query` и конструкции join fetch

Если в запросе указать `join fetch` (вместо просто `join`), то Hibernate в блок `select` включит поля из `join` и
размапит результат в связанную сущность.

```java
public interface PersonRepository
        extends JpaRepository<Person, Integer> {

    @Query("select p from Person p join fetch p.address")
    List<Person> findPersonAndAddress();
}

@Service
@RequiredArgsConstructor
public class PersonServiceImpl
        implements PersonService {
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    @Override
    public List<PersonResponse> findAll() {
        return personRepository.findPersonAndAddress()
                .stream()
                .map(personMapper::toModel)
                .collect(Collectors.toList());
    }
}
```

##### Использовать EntityGraph для конкретного метода

Начиная с версии JPA 2.1 появилась конструкция `@EntityGraph`, с помощью которой можно переопределять порядок загрузки
сущностей, описанных в `@Entity`. Т.е. если в `@Entity` описано:

```java

@Entity
@Table(name = "person")
public class Person {

    ...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", foreignKey = @ForeignKey(name = "fk_person_address_id"))
    private Address address;
    
    ...
}
```

а в запросе указано `@EntityGraph(attributePaths = "address")`, то в едином запросе будет подняты сущности Person и
Address.

```java
public interface PersonRepository
        extends JpaRepository<Person, Integer> {

    @EntityGraph(attributePaths = "address")
    @Query("select p from Person p")
    List<Person> findAllUsingGraph();
}

@Service
@RequiredArgsConstructor
public class PersonServiceImpl
        implements PersonService {
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    @Override
    public List<PersonResponse> findAll() {
        return personRepository.findAllUsingGraph()
                .stream()
                .map(personMapper::toModel)
                .collect(Collectors.toList());
    }
}
```

### Запуск приложения

```shell
# сборка проекта
$ ./gradlew clean build

# запуск postgres 13 в docker
$ docker compose up -d

# запуск приложения
$ ./gradlew bootRun

# выполняем запрос
$ curl http://localhost:8080 -v | jq
```