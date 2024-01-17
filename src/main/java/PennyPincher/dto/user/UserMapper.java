package PennyPincher.dto.user;

import PennyPincher.entity.Role;
import PennyPincher.entity.User;
import PennyPincher.repository.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public User mapToDomain(UserDto userDto) {
        Role role = roleRepository.findByRole("ROLE_USER");
        if (role == null) {
            role = checkRoleExist();
        }

        return User.builder()
                .firstName(userDto.getFirstName())
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .roles(List.of(role))
                .build();
    }

    private Role checkRoleExist() {
        Role role = new Role();
        role.setRole("ROLE_USER");
        return roleRepository.save(role);
    }

}
