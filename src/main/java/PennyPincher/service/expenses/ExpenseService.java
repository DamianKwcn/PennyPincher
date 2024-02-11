package PennyPincher.service.expenses;

import PennyPincher.entity.Expense;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public interface ExpenseService {
    void save(Expense expense);
    void deleteById(Integer expenseId);
    void deleteByEventId(Integer eventId);
    Expense findById(Integer expenseId);
    List<Expense> findExpensesForGivenEvent(Integer eventId);
    BigDecimal splitCostEquallyPerParticipants(BigDecimal amount, long participantsNumber);
    Map<Integer, BigDecimal> mapUserToPayoffAmount(Expense expense);
    Map<Integer, BigDecimal> mapUserToBalance(Expense expense);
    Expense findByExpenseNameAndEventId(String expenseName, Integer eventId);
}
