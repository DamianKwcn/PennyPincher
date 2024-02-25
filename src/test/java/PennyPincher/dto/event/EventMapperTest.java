package PennyPincher.dto.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import PennyPincher.entity.Event;
import PennyPincher.entity.User;
import PennyPincher.service.users.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
public class EventMapperTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private EventMapper eventMapper;

    @Test
    public void Should_MapEventToDomain() {
        // given
        EventDto eventDto = new EventDto();
        eventDto.setEventName("event");

        User owner = new User();
        owner.setId(1);
        owner.setUsername("test_user");
        owner.setFirstName("user1");

        when(userService.getCurrentlyLoggedInUser()).thenReturn(owner);

        // when
        Event event = eventMapper.mapToDomain(eventDto);

        // then
        assertNotNull(event);
        assertEquals("Event", event.getEventName());
        assertEquals(owner, event.getOwner());
        assertEquals(LocalDate.now(), event.getCreationDate());
    }
}
