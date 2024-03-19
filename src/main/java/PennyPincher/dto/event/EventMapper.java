package PennyPincher.dto.event;

import PennyPincher.entity.Event;
import PennyPincher.entity.User;
import PennyPincher.exception.UserNotFoundException;
import PennyPincher.service.users.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@AllArgsConstructor
public class EventMapper {

    private final UserService userService;

    public Event mapToDomain(EventDto eventDto, @AuthenticationPrincipal UserDetails userDetails) {
        User owner = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Currently logged in user not found."));

        return Event.builder()
                .eventName(getEventName(eventDto))
                .creationDate(LocalDate.now())
                .owner(owner)
                .build();
    }

    private static String getEventName(EventDto eventDto) {
        return Character.toUpperCase(eventDto.getEventName().charAt(0)) + eventDto.getEventName().substring(1);
    }
}
