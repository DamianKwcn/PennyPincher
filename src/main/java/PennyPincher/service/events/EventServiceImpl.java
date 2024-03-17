package PennyPincher.service.events;

import PennyPincher.entity.Event;
import PennyPincher.entity.User;
import PennyPincher.exception.EventNotFoundException;
import PennyPincher.repository.EventRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    public Event findById(Integer eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));
    }

    @Override
    public Optional<Event> findByEventNameAndOwner(String eventName, User owner) {
        return eventRepository.findByEventNameAndOwner(eventName, owner);
    }

    @Override
    public void save(Event event) {
        eventRepository.save(event);
    }

    @Override
    public void deleteById(Integer eventId) {
        eventRepository.deleteById(eventId);
    }

    @Override
    public Optional<List<Event>> findEventsByUser(User user) {
        return eventRepository.findEventsByOwnerOrEventMembers(user, user);
    }
}
