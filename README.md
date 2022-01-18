# JPA example

### spring.jpa.open-in-view

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

#### Способы исправления

1. Использование `@Transactional` на сервисы, тогда подзапросы будут выполняться в рамках сессии:
   
   ```java
    @Override
    @Transactional(readOnly = true)
    public List<PersonResponse> findAll() {
        return personRepository.findAll()
                .stream()
                .map(personMapper::toModel)
                .collect(Collectors.toList());
    }
   ```
   
2. Использование `@Query` и конструкции join fetch:
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
   
3. Описывать EntityGraph для конкретного метода:
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
   
### Запуск и проверка

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