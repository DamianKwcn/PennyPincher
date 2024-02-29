package PennyPincher.dto.expense;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CustomExpenseDto {

    @NotBlank
    private String name;

    @NotBlank
    private String cost;

    private List<String> participantsNames;

    private Map<Integer, BigDecimal> userContribution;
}
