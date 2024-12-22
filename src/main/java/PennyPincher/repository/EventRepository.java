package PennyPincher.repository;

import PennyPincher.model.Event;
import PennyPincher.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Integer> {
    Optional<Event> findByEventNameAndOwner(String eventName, User owner);

    @EntityGraph(attributePaths = {"eventMembers"})
    Optional<List<Event>> findEventsByOwnerOrEventMembers(User user, User member);
}
