package PennyPincher.service.events;

import PennyPincher.entity.Event;
import PennyPincher.entity.User;
import PennyPincher.repository.EventRepository;
import PennyPincher.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;

    @Override
    public List<Event> findAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public void save(Event event) {
        eventRepository.save(event);
    }

    @Override
    public Event findById(Integer eventId) {
        return eventRepository.findById(eventId).orElseThrow();
    }

    @Override
    public Event findByEventNameAndOwner(String eventName, User owner) {
        return eventRepository.findByEventNameAndOwner(eventName, owner);
    }

    @Override
    public void deleteById(Integer eventId) {
        eventRepository.deleteById(eventId);
    }
}
