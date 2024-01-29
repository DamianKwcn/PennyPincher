package PennyPincher.service.events;

import PennyPincher.entity.Event;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EventService {
    void save(Event event);
    void deleteById(Integer eventId);
    List<Event> findAllEvents();
    Event findById(@NotEmpty Integer eventId);
    Event findByEventName(String eventName);
    List<Event> findEventsByName(String eventName);
}
