package PennyPincher.service.events;

import PennyPincher.model.Event;
import PennyPincher.model.User;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface EventService {
    void save(Event event);
    void deleteById(Integer eventId);
    Event findById(@NotEmpty Integer eventId);
    Optional<Event> findByEventNameAndOwner(String eventName, User owner);
    Optional<List<Event>> findEventsByUser(User user);
    void populateUserLists(List<User> allUsers, List<User> eventMembers, List<User> remainingUsers);
}
