package PennyPincher.dto.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDto {

    private int idUser;

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    }