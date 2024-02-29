package PennyPincher.dto.event;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EventDto {

    @NotBlank
    private String eventName;
}
