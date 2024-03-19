package PennyPincher.service.event;

import PennyPincher.entity.Event;
import PennyPincher.entity.User;
import PennyPincher.exception.EventNotFoundException;
import PennyPincher.repository.EventRepository;
import PennyPincher.service.events.EventServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event event;
    private User user;

    @Before
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

    @Test(expected = EventNotFoundException.class)
    public void Should_FindById_NonExistingEvent() {
        // given
        when(eventRepository.findById(2)).thenReturn(Optional.empty());

        // then
        eventService.findById(2);
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
        User user1 = new User();
        User user2 = new User();
        user1.setId(1);
        user2.setId(2);
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
