package ru.romanow.jpa.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.romanow.jpa.domain.Authority;
import ru.romanow.jpa.model.AuthorityInfo;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(config = MapperConfiguration.class)
public interface AuthorityMapper {
    AuthorityInfo toModel(Authority grant);

    @Mapping(target = "person", ignore = true)
    Authority toEntity(AuthorityInfo authorityInfo);

    default Set<Authority> fullUpdate(Set<Authority> newAuthorities, @MappingTarget Set<Authority> authorities) {
        //  NOTE
        // если обновить через очистку
        // authorities.clear();
        // authorities.addAll(newAuthorities);
        // то можно получить ошибку
        // detached entity passed to persist: ru.romanow.jpa.domain.Authority;
        // nested exception is org.hibernate.PersistentObjectException: detached entity passed to persist: ru.romanow.jpa.domain.Authority
        // Это происходит из-за того, что сначала удаляются (detach), а потом добавляются(persist)  эти же сущности. А из detach можно вернуться
        // только через merge

        var newAuthoritiesMap = newAuthorities
                .stream()
                .filter(authority -> Objects.nonNull(authority.getId()))
                .collect(toMap(Authority::getId, identity()));
        final var difference = authorities.stream()
                .filter(authority -> !newAuthoritiesMap.containsKey(authority.getId()))
                .collect(Collectors.toSet());
        authorities.removeAll(difference);

        var authorityMap = authorities
                .stream()
                .collect(toMap(Authority::getId, identity()));
        for (Authority newAuthority : newAuthorities) {
            Optional.ofNullable(authorityMap.get(newAuthority.getId()))
                    .ifPresentOrElse(
                            authority -> fullUpdate(newAuthority, authority),
                            () -> {
                                final var authority = fullUpdate(newAuthority, new Authority());
                                authorities.add(authority);
                            });
        }
        return authorities;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "person", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void update(AuthorityInfo request, @MappingTarget Authority authority);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "person", ignore = true)
    void fullUpdate(AuthorityInfo request, @MappingTarget Authority authority);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "person", ignore = true)
    Authority fullUpdate(Authority newAuthority, @MappingTarget Authority authority);
}