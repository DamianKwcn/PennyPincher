package PennyPincher.repository;

import PennyPincher.entity.EventMembers;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventMembersRepository extends JpaRepository<EventMembers, Integer> {
    EventMembers findByEventId(Integer eventId);
}