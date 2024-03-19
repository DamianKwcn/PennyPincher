package PennyPincher.repository;

import PennyPincher.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
    Expense findByNameAndEventId(String expenseName, Integer eventId);
    List<Expense> findByEventId(Integer eventId);
}
