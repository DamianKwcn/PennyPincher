package PennyPincher.service.event;

import PennyPincher.model.Event;
import PennyPincher.model.User;
import PennyPincher.exception.EventNotFoundException;
import PennyPincher.repository.EventRepository;
import PennyPincher.service.events.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventService;
    private Event event;
    private User user;

    User user1 = User.builder().id(1).firstName("user1").username("user1").password("password").build();
    User user2 = User.builder().id(2).firstName("user2").username("user2").password("password").build();

    @BeforeEach
    public void setUp() {
        event = new Event();
        event.setId(1);

        user = new User();
        user.setId(1);
    }
    @Test
    public void Should_FindById_ExistingEvent() {
        // given
        when(eventRepository.findById(1)).thenReturn(Optional.of(event));

        // when
        Event result = eventService.findById(1);

        // then
        assertEquals(event, result);
    }

    @Test
    public void should_FindById_NonExistingEvent() {
        // given
        when(eventRepository.findById(2)).thenThrow(new EventNotFoundException("Event not found with ID: 2"));

        // when
        assertThrows(EventNotFoundException.class, () -> eventService.findById(2));
    }

    @Test
    public void Should_FindByEventNameAndOwner() {
        // given
        when(eventRepository.findByEventNameAndOwner("TestEvent", user)).thenReturn(Optional.of(event));

        // when
        Optional<Event> result = eventService.findByEventNameAndOwner("TestEvent", user);

        // then
        assertEquals(event, result.orElse(null));
    }

    @Test
    public void Should_DeleteById() {
        // when
        eventService.deleteById(1);

        // then
        verify(eventRepository, times(1)).deleteById(1);
    }

    @Test
    public void Should_FindEventsByUser() {
        // given
        List<Event> events = new ArrayList<>();
        events.add(event);
        when(eventRepository.findEventsByOwnerOrEventMembers(user, user)).thenReturn(Optional.of(events));

        // when
        Optional<List<Event>> result = eventService.findEventsByUser(user);

        // then
        assertEquals(events, result.orElse(null));
    }

    @Test
    public void Should_PopulateUserLists() {
        // given
        List<User> allUsers = new ArrayList<>();
        List<User> eventMembers = new ArrayList<>();
        List<User> remainingUsers = new ArrayList<>();
        eventMembers.add(user1);
        allUsers.add(user1);
        allUsers.add(user2);

        // when
        eventService.populateUserLists(allUsers, eventMembers, remainingUsers);

        // then
        assertEquals(1, remainingUsers.size());
        assertEquals(user2, remainingUsers.get(0));
    }

}
