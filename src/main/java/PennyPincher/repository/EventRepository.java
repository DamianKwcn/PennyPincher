package PennyPincher.repository;

import PennyPincher.entity.Event;
import PennyPincher.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Integer> {
    Event findByEventNameAndOwner(String eventName, User owner);
}
