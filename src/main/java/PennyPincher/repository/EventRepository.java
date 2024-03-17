package PennyPincher.repository;

import PennyPincher.entity.Event;
import PennyPincher.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Integer> {
    Optional<Event> findByEventNameAndOwner(String eventName, User owner);
    Optional<List<Event>> findEventsByOwnerOrEventMembers(User user, User member);
}
