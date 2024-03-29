package PennyPincher.dto.expense;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SplitExpenseDto {

    @NotBlank
    private String name;

    @NotBlank
    private String cost;

    @NotBlank
    private String participantUsername;

}
