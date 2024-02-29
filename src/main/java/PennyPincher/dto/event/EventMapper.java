package PennyPincher.dto.event;

import PennyPincher.entity.Event;
import PennyPincher.service.users.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@AllArgsConstructor
public class EventMapper {

    private final UserService userService;

    public Event mapToDomain(EventDto eventDto) {
        return Event.builder()
                .eventName(getEventName(eventDto))
                .owner(userService.getCurrentlyLoggedInUser())
                .creationDate(LocalDate.now())
                .build();
    }

    private static String getEventName(EventDto eventDto) {
        return Character.toUpperCase(eventDto.getEventName().charAt(0)) + eventDto.getEventName().substring(1);
    }
}
