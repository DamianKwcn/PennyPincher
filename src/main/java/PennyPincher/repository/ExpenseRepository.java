package PennyPincher.repository;

import PennyPincher.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
    Expense findByNameAndEventId(String expenseName, Integer eventId);
}
