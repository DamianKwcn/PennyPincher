package PennyPincher.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDto {

    private int idUser;

    @NotBlank
    private String firstName;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    }