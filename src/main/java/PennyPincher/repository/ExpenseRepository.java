package PennyPincher.repository;

import PennyPincher.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
    Expense findByNameAndEventId(String expenseName, Integer eventId);
    List<Expense> findByEventId(Integer eventId);
}
