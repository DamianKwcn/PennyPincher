package PennyPincher.dto.event;

import PennyPincher.entity.Event;
import PennyPincher.entity.User;
import PennyPincher.service.users.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
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

        // when
        when(userService.findByUsername("test_user")).thenReturn(java.util.Optional.of(owner));
        Event event = eventMapper.mapToDomain(eventDto,
                new org.springframework.security.core.userdetails.User("test_user", "", null));

        // then
        assertNotNull(event);
        assertEquals("Event", event.getEventName());
        assertEquals(owner, event.getOwner());
        assertEquals(LocalDate.now(), event.getCreationDate());
    }
}
