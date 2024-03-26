package PennyPincher.dto.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import PennyPincher.dto.user.UserDto;
import PennyPincher.dto.user.UserMapper;
import PennyPincher.entity.Role;
import PennyPincher.entity.User;
import PennyPincher.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class UserMapperTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserMapper userMapper;

    @Test
    public void Should_MapUserToDomain() {
        // given
        UserDto userDto = new UserDto();
        userDto.setFirstName("user1");
        userDto.setUsername("user1");
        userDto.setPassword("password");

        Role role = new Role();
        role.setId(1);
        role.setRole("ROLE_USER");

        // when
        when(roleRepository.findByRole("ROLE_USER")).thenReturn(role);
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn("encodedPassword");
        User user = userMapper.mapToDomain(userDto);

        // then
        assertNotNull(user);
        assertEquals("user1", user.getFirstName());
        assertEquals("user1", user.getUsername());
        assertEquals("encodedPassword", user.getPassword());
        assertEquals(BigDecimal.ZERO, user.getBalance());
        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().contains(role));
    }
}
