package ru.romanow.jpa.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.romanow.jpa.domain.Authority;

import java.util.HashSet;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class AuthorityMapperTest {

    private AuthorityMapper authorityMapper;

    @BeforeEach
    void setUp() {
        this.authorityMapper = new AuthorityMapperImpl();
    }

    @Test
    void shouldAddNotExistedAuthoritiesWhenFullUpdate() {
        final var authority1 = new Authority().setId(1).setName("1");
        final var authority2 = new Authority().setId(2).setName("2");
        final var authorities = new HashSet<Authority>();
        authorities.add(authority1);
        authorities.add(authority2);
        final var newAuthorities = new HashSet<>(authorities);
        final var authority3 = new Authority().setName("3");
        newAuthorities.add(authority3);

        final var updatedAuthorities = authorityMapper.fullUpdate(newAuthorities, authorities);

        assertThat(updatedAuthorities).containsExactlyElementsOf(newAuthorities);
    }

    @Test
    void shouldUpdateExistedAuthorityWhenFullUpdate() {
        final var authority1 = new Authority().setId(1).setName("1");
        final var authority2 = new Authority().setId(2).setName("2");
        final var authorities = new HashSet<Authority>();
        authorities.add(authority1);
        authorities.add(authority2);
        final var newAuthorities = new HashSet<Authority>();
        final var updatedAuthority2 = new Authority().setId(authority2.getId()).setName(authority2.getName() + "_update");
        newAuthorities.add(authority1);
        newAuthorities.add(updatedAuthority2);

        final var updatedAuthorities = authorityMapper.fullUpdate(newAuthorities, authorities);

        assertThat(updatedAuthorities).containsExactly(authority1, updatedAuthority2);
    }

    @Test
    void shouldDeleteExistedAuthorityWhenIsFullUpdated() {
        final var authority1 = new Authority().setId(1).setName("1");
        final var authority2 = new Authority().setId(2).setName("2");
        final var authorities = new HashSet<Authority>();
        authorities.add(authority1);
        authorities.add(authority2);
        final var newAuthorities = new HashSet<>(authorities);
        newAuthorities.remove(authority2);

        final var updatedAuthorities = authorityMapper.fullUpdate(newAuthorities, authorities);

        assertThat(updatedAuthorities).containsExactlyElementsOf(newAuthorities);
    }

}