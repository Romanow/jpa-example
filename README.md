[![CI](https://github.com/Romanow/jpa-example/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/Romanow/jpa-example/actions/workflows/build.yml)
[![pre-commit](https://img.shields.io/badge/pre--commit-enabled-brightgreen?logo=pre-commit)](https://github.com/pre-commit/pre-commit)
[![License](https://img.shields.io/github/license/Romanow/jpa-example)](https://github.com/Romanow/jpa-example/blob/main/LICENSE)

# MapStruct vs. Hibernate: да придет спаситель!

## Аннотация

Наверняка вы много раз сталкивались с тем, что при работе Hibernate вам приходилось писать много кода по перекладыванию
из `@Entity` в DTO и обратно. Это громоздкий некрасивый boilerplate код, к тому же подверженный ошибкам. И вот, казалось
бы появился спаситель – MapStruct! С помощью codegen он забирает на себя всю рутинную работу по перекладке, а вы лишь
вызываете готовый метод. Но так ли все просто и радужно? В докладе поговорим про сложные случаи, когда нам надо
создавать и обновлять сущности, имеющие связи на другие объекты.

## План

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

## Доклад

### Настройка JPA spring.jpa.open-in-view=false

> Spring web request interceptor that binds a JPA EntityManager to the thread for the entire processing of the request.
> Intended for the "Open EntityManager in View" pattern, i.e. to allow for lazy loading in web views despite the
> original
> transactions already being completed.

Класс `OpenEntityManagerInViewInterceptor` в методе `preHandle` открывает `EntityManager` для текущего запроса, т.е.
Spring создает обрамляющую транзакцию на _весь_ запрос.

Выключение этого параметра (`spring.jpa.open-in-view=false`) приведет к тому, что инициировать транзакцию для работы со
смежными данными нужно будет руками.

Это правильный подход, т.к. он дает контроль над транзакционной целостью запроса.

Если использовать `CrudRepository` (или его наследников), то Spring в runtime в proxy подкладывает реализацию
`SimpleJpaRepository`, которая помечена аннотацией `@Transasctional(readOnly = true)` на уровне класса, т.е. транзакция
создается на каждый запрос.

### В коде используем явное управление транзакциями через `@Transactional`

Использование транзакций гарантирует:

* Атомарность (Atomicity) – гарантирует, что никакая транзакция не будет зафиксирована в системе частично. Будут либо
  выполнены все операции внутри транзакции, либо не выполнено ни одной.
* Консистентность (Consistency) – транзакция, достигающая своего нормального завершения и, тем самым, фиксирующая свои
  результаты, сохраняет согласованность базы данных.
* Изолированность (Isolation) – гарантирует что никакой поток данных не может читать данные из еще не завершенной
  транзакции.
* Долговечность (Durability) – если транзакция завершена, то все данные записаны на диск.

Если в рамках запроса выполняется модификация нескольких таблиц, то без использования общей транзакции в случае ошибки
откат изменений не будет выполнен или будет выполнен частично, что приведет к _неконсистентности_ данных.

В postgres уровень изоляции по-умолчанию READ COMMITED, т.е. гарантирует отсутствие Lost Updates и Dirty Reads.

Т.к. операции в бизнес сценарии часто подразумевают изменения в нескольких таблицах, то все эти изменения нужно
заворачивать в единую транзакцию, чтобы достичь консистентности данных.

Если брать классическое Spring Boot приложение со Spring MVC, то выделяется три главных части:

* web: `@Controller`, `@ControllerAdvice`, `Filter`, и т.п. – уровень представления, здесь находится описание API.
* service: `@Service`, `@Component` – бизнес логика приложения.
* dao: `@Entity`, `@Repository`, `CrudRepository`, `JpaRepository` и т.п. – слой доступа к данным.

Транзакции нужно использовать на уровне service, т.к. именно там находится бизнес-логика приложения и именно этот слой
ответственен за корректность (консистентность) работы с данными.

Уровень web является представлением и его задача – описание API, а значит бизнес логики (а значит и транзакций) на этом
уровне быть _не должно_.

Уровень dao является слоем доступа к данными, здесь обычно описываются _отдельные_ обращения к БД, а значит оборачивать
их в транзакцию бессмысленно.

Получается что использование транзакций должно находится на уровне service, т.к. на этом слое находится бизнес логика
приложения.

Рассмотрим подробнее разбиение бизнес функционала по сервисам. Если в сервисе выделяется больше одной доменной области,
например, User и Wallet, то все классы (web, mappings, models, dao, services), связанные с ними, должны находится в
отдельном пакете user и wallet соответственно.

```text
src/
  main/
    java/
      ru/vtb/
        user/
          dao/
          models/
          services/
          web/
        wallet/
          dao/
          models/
          services/
          web/
```

* Для того, чтобы сервис (`@Service`) был изолированный, он должен взаимодействовать только с DAO и репозиториями из
  своего домена. Т.е. если нам в `WalletService` нужно получить пользователя, то мы должны использовать `UserService`, а
  не работать напрямую с `UserRepository`. Иначе нарушается Single Responsibility принцип и сильно усложняются unit
  тесты.
* Если есть какие-то общие классы, сервисы, то они выносятся в пакет common (например, `@RestControllerAdvice`).

Разбиение по доменным сущностям:

* Доменная область обычно 1 к 1 связана с бизнес-процессом, т.е. у вас в одном пакете есть контроллеры (и сервисы),
  которые выполняют разную функциональность из разных Use Case (работа с пользователем (создание, блокировка) и работа с
  кошельком (создание, пополнение, закрытие)), то это обычно различные доменные области.
* Если у вас есть необходимость в sql / jpa запросе использовать join на таблицы из разных _независимых_ доменных
  областей, то лучше это делать в java коде, потому что в случае дальнейшего распила сервиса на части, сущности из этого
  join могут начать относиться к разным сервисам, а значит join придется распиливать. Другими словами, если у нас есть
  отношение User -> Address, причем Address не может существовать без User, то для этих сущностей можно и нужно
  использовать join, т.к. они в одной доменной области user. А если у нас есть User и Wallet, то эти сущности уже из
  разных доменных областей и использование join может усложнить дальнейших рефакторинг.
* К одной доменной сущности могут относится объекты, которые будут невалидны без основной сущности. Например, User ->
  Address, адрес будет невалиден без привязки к пользователю, но Address -> Country, Address -> City уже не будут в
  одном домене, т.к. Country и City могут потребоваться в других процессах.

И вообще основное правило всего - поддерживать структуру сервисов, мапперов, репозиториев в соответствии с доменной
моделью.

Если в рамках бизнес операции используются только запросы на чтение, то нужно в транзакции
указать `@Transactional(readOnly = true)`.

Аннотацию `@Transactional` нужно указывать в реализации и лучше аннотировать ей каждый метод, где это нужно.
Помечать `@Transactional` декларацию методов в интерфейсе не стоит, т.к. это выдает детали внутренней реализации и, если
в реализации этой аннотации не будет, то по факту транзакция создастся (т.к. Spring увидит `@Transactional` в
интерфейсе), но по коду это будет неочевидно.

##### Использование автогенерации схем данных JPA в прод среде запрещено

Автогенерация DDL занимает много времени, т.к. Hibernate через метаинформацию вытягивает структуру БД и сравнивает ее с
описанием в `@Entity`.

Правильным и контролируемым подходом для работы со схемой базы данных являются скрипты миграции. Для Java есть два
основных инструмента:

* [Flyway](https://flywaydb.org/documentation/usage/plugins/springboot), интеграция со Spring
  Boot [Use a Higher-level Database Migration Tool](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto.data-initialization.migration-tool)
  .
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

* `@OneToOne` (EAGER) – связь 1:1, реализуется через Foreign Key, реализовать LAZY без отдельных костылей нельзя.
* `@OneToMany` (LAZY) – возвратный ключ, указывает на список записей, которые ссылаются через Foreign Key на текущую
  запись. Делать связь EAGER плохая практика, т.к. на каждый запрос будет подниматься большое количество лишних записей.
  Если в каком-то случае нужны все записи, то можно использовать `join fetch` или `@EntityGraph`.
* `@ManyToOne` (EAGER) – прямой ключ на запись, в описании указывается `@JoinColumn`. Если эта связь не нужна во всех
  запросах, то лучше ее тоже делать LAZY, а поднимать только в случае необходимости.
* `@ManyToMany` (LAZY) – связь многое-ко-многим, реализуется через смежную таблицу. Делать EAGER нельзя, т.к. это
  свидетельствует о плохо спроектированной базе данных.

Изменение типа связи с LAZY на EAGER _крайне_ не рекомендуется, это может очень негативно сказаться на
производительности, т.к. при поднятии одной сущности, будут подниматься еще N дополнительных сущностей.

При этом, если связь помечена LAZY, а обращение к ней выполняется вне транзакции, то будет
выброшен `LazyInitializationException` (подробнее в примерах). Для предотвращения такой ситуации нужно явно использовать
транзакции и (или) использовать `join fetch` и `@EntityGraph` в случае, когда эти данные нужны в получаемом результате.

### Пояснения и комментарии

#### Использование MapStruct

Отдавать в ответе сервиса сущность `@Entity` очень плохая практика, т.к. это приводит к некотролируемому поведению
приложения. Создают специальные сущности, именуемые DTO (Data Transfer Object), которые служат моделями для запросов /
ответов.

Это в свою очередь приводит к необходимости писать мапперы в/из DTO из/в `@Entity`. Часто для решения этой проблемы
используют библиотеку [MapStruct](https://mapstruct.org/documentation/stable/reference/html/). Но при сложных объектах
она может сильно усложнить и запутать код и привести к лишним запросам в базу данных при маппинге.

Задача маппера просто переложить готовые данные из одного объекта в другой. Маппер – это сервис в рамках многослойной
архитектуры нашего приложения, а значит он содержит бизнес логику, а следовательно его нужно тестировать.

Мапперы следует делать быть максимально простыми, в идеальном случае они должны заниматься только перекладыванием
плоских полей (`String`, `Integer`, `BigDecimal`) из объекта в объект. Если объект является составным, то для каждой
доменной сущности должен быть написан свой маппер. Структурная зависимость мапперов должна повторять структуру доменной
области для простоты понимания, поддержки и соблюдения Single Responsibility Principle.

Весь маппинг строить в виде "звезды" _от доменной сущности_, то есть, избегать маппинг DTO1 -> DTO2, – это упрощает
поддержку быстроменяющихся DTO. Также могут получиться такие зависимости DTO1 -> DTO2 -> `@Entity`, тогда придется
поддерживать DTO2 даже если он уже не используется, или удалять его с переписыванием маппера в DTO1.

Для сложных составных объектов нужно разделять операции создания и редактирования
(рассматриваем маппинг DTO -> `@Entity`):

* Для создания можно полностью использовать маппер, а на уровне `@Entity` на поля `@OneToMany`, `@ManyToOne`,
  `@ManyToMany` проставить `cascade = CascadeType.PERSIST`, чтобы Hibernate по цепочке создал вложенные объекты и
  привязал их к основному. Т.к. здесь используется Hibernate, эта операция должны выполняться в транзакции.
* Операцию обновления сложной сущности нужно делать руками, используя MapStruct только для перекладывания плоских полей.
    * Если требуется обновить сущность `@ManyToOne`, то просто переходим в связную сущность и обновляем в ней
      необходимые поля, с помощью `cascade = CascadeType.MERGE` Hibernate выполнит обновление связной сущности.
    * Если выполняется частичное обновление (метод PATCH) и требуется обновить массив записей `@OneToMany`, значит надо
      по ID из массива получить нужную запись для обновления. Как и в примере выше, с
      помощью `cascade = CascadeType.MERGE` Hibernate выполнит обновление связной сущности при сохранении.
    * Если выполняется полное обновление (метод PUT), то среди существующих записей ищутся все записи по ID из запроса:
        * найденные записи обновляются (с помощью `cascade = CascadeType.MERGE` Hibernate их обновит);
        * с отсутствующих о записях убирается связь с главной сущностью и с помощью `orphanRemoval = true` Hibernate их
          удаляет;
        * новые сущности, которые есть в запросе, просто создаются и помощью `cascade = CascadeType.MERGE` Hibernate их
          создаст.

В случае использования `cascade` нужно в явном виде перечислять
операции: `{ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH }`. Тип `CascadeType.ALL`
включает `CascadeType.REMOVE`, который каскадно удаляет все связанные записи.

Для обновления возникнет ошибка:

> org.springframework.dao.InvalidDataAccessApiUsageException: org.hibernate.TransientPropertyValueException:
> object references an unsaved transient instance - save the transient instance before flushing :
> ru.romanow.jpa.domain.Person.address -> ru.romanow.jpa.domain.Address; nested exception is
> java.lang.IllegalStateException: org.hibernate.TransientPropertyValueException: object references an unsaved
> transient instance - save the transient instance before flushing : ru.romanow.jpa.domain.Person.address ->
> ru.romanow.jpa.domain.Address

Так же при удалении объекта могут потребоваться удалять все подчиненные сущности, а значит нужно будет
использовать `orphanRemoval = true`.

```java

@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface OneToMany {

    ...

    /**
     * (Optional) Whether to apply the remove operation to entities that have
     * been removed from the relationship and to cascade the remove operation to
     * those entities.
     */
    boolean orphanRemoval() default false;
}
```

##### Правила работы с Mapper

* Для связи мапперов использовать `uses`: `@Mapper(uses = { AddressMapper.class }`.
* Если ваше приложение написано на Spring Boot, то мапперы тоже должны быть под управлением
  Spring: `@Mapper(componentModel = "spring")`.
* Для создания мапперов
  использовать `@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)`, что позволит
  создавать маппер в тестах без создания контекста Spring.
* Нужно избегать внедрения других сервисов в маппер, это упростит поддержку. Лучше сделать что маппер сможет, а
  остальное уже доставить в сервисе вызова и туда внедрить зависимости.
* Не рекомендуется использовать `@BeforeMapping`, так как входная (source) сущность может находиться в Persistence
  Context и её изменения в рамках метода `@BeforeMapping` могут быть неявно сохранены.
* Не использовать `expression="java()"` для методов с бизнес логикой – это очень сложно тестировать.
* Полезно использовать `@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)`, а все неиспользуемые поля в маппинге
  явно указывать через `ignore = true`. Это позволит не пропустить поля в итоговом объекте.

#### Выполнение внешних вызовов из сервиса, помеченного `@Transcational`

##### REST запрос

Если в рамках бизнес операции, реализуемой в методе на уровне сервиса, присутствует вызов ко внешней системе (HTTP
запрос, gRPC и т.п.), то заворачивать этот метод в транзакцию не стоит, т.к. транзакция будет ждать завершения всей
операции.

С другой стороны это удобно, т.к. в случае негативного ответа 4xx/5xx будет выброшен exception и вся транзакция
откатится. Этот подход можно использовать только если есть _маленький_ таймаут (200-300ms) на завершение внешнего
вызова. В PostgreSQL работа с транзакциями реализуется с помощью версионирования (snapshot), это не блокирует
параллельные транзакции, но может привести к rollback в случае если данные были модифицированы в рамках другой ранее
завершенной транзакции.

Если время работы внешнего вызова не фиксировано или большое, то его нужно делать вне транзакции, т.е. мы разбиваем нашу
бизнес-операцию на две транзакционные части, а внешний вызов выполняется между этими транзакциями. Таким образом, если
вызов завершился с ошибкой 4xx/5xx, то мы должны _руками_ откатить изменения в первой части бизнес операции.

##### Отправка данных через очередь

Очередь является инструментом асинхронного взаимодействия. Если в рамках бизнес операции требуется отправить данные в
очередь, то если эта операция выполняется в рамках транзакции, может произойти ситуация, что получатель
(consumer) получит заявку до того момента, как на отправителе (producer) завершится транзакция, что может привести к
неконсистентным данным между отправителем и получаетелем в момент выполнения операции.

Для решения этой ситуации можно следовать подходу, описанному выше: выносить отправку данных из транзакции, либо в
заявку, отправляемую в очередь, класть все данные, чтобы получателю не было необходимости приходить за дополнительной
информацией к отправителю. Но здесь стоит помнить, что очередь не предназначена для отправки больших объемов данных:
сообщение в 5-10Kb – ОК, а вот файл или json размером в 1Mb уже плохо.

## Примеры

Параметр `spring.jpa.open-in-view` маппируется в класс `JpaProperties`.

```java

@ConfigurationProperties(prefix = "spring.jpa")
public class JpaProperties {
    ...

    /**
     * Register OpenEntityManagerInViewInterceptor. Binds a JPA EntityManager to the
     * thread for the entire processing of the request.
     */
    private Boolean openInView;

    ...
}
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

Если метод в сервисе пометить аннотацией `@Transactional`, тогда подзапросы будут выполняться в рамках сессии:

```java

@Service
@RequiredArgsConstructor
public class PersonServiceImpl
    implements PersonService {
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PersonResponse> findAll() {
        return personRepository.findAll()
            .stream()
            .map(personMapper::toModel)
            .collect(Collectors.toList());
    }
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

##### Использование `@Column` при поиске по ID поля, помеченного `@ManyToOne`

Если для запросов сущностей, помеченных `@ManyToOne` нужно поднять сущность по ID, то можно рядом с `@ManyToOne` описать
сам ID:

```java

@Entity
@Table(name = "person")
public class Person {

    ...

    @Column(name = "address_id", updatable = false, insertable = false)
    private Integer addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", foreignKey = @ForeignKey(name = "fk_person_address_id"))
    private Address address;

    ...
}
```

```jpaql
select u.name from User u where u.addressId = :addressId
```

### Запуск приложения

```shell
# сборка проекта
$ ./gradlew bootRun --

$ docker compose up -d --wait

# запуск приложения
$ ./gradlew bootRun

# выполняем запрос
$ curl http://localhost:8080/api/v1/persons -v | jq
```

### Особенности реализации

1. `@EntityGraph` по-умолчанию `type = EntityGraphType.FETCH`, это значит что описанные сущности Hibernate поднимает как
   EAGER, а все остальные считает как LAZY (даже если в `@Entity` они описаны как EAGER). `EntityGraphType.LOAD` берет
   из описания `@Entity`.
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
4. Для удаления старых записей при обновлении объекта `@OneToMany` нужно сделать:

    ```jshelllanguage
    person.getAuthorities().clear();
    person.getAuthorities().addAll(newAuthorities);
    ```
