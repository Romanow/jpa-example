package ru.romanow.jpa.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.romanow.jpa.web.utils.EntityBuilder;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonMapperTest {

    private PersonMapper personMapper;
    @Mock
    private AuthorityMapper authorityMapper;
    @Mock
    private AddressMapper addressMapper;
    @Mock
    private RoleMapper roleMapper;

    @BeforeEach
    void setUp() {
        this.personMapper = new PersonMapperImpl(addressMapper, authorityMapper, roleMapper);
    }

    @Test
    void shouldMapAllNested() {
        final var request = EntityBuilder.buildPersonModifyRequest(3);

        final var person = this.personMapper.toModel(request);

        assertThat(person).isNotNull();
        then(authorityMapper).should(atLeastOnce()).toEntity(any());
        then(addressMapper).should().toEntity(any());
        then(roleMapper).should(atLeastOnce()).toEntity(any());
    }

}