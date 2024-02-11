package PennyPincher.service.events;

import PennyPincher.entity.Event;
import PennyPincher.repository.EventRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
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
    public Event findByEventName(String eventName) {
        return eventRepository.findByEventName(eventName);
    }

    @Override
    public List<Event> findEventsByName(String eventName) {
        return eventRepository.findByEventNameContainingIgnoreCase(eventName);
    }

    @Override
    public void deleteById(Integer eventId) {
        eventRepository.deleteById(eventId);
    }

}
