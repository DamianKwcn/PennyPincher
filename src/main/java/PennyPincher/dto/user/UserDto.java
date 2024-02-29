package PennyPincher.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDto {

    @JsonIgnore
    private int idUser;

    @NotBlank
    private String firstName;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    }