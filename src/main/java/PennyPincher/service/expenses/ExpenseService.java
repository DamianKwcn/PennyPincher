package PennyPincher.service.expenses;

import PennyPincher.dto.expense.CustomExpenseDto;
import PennyPincher.dto.expense.ExpenseMapper;
import PennyPincher.entity.Event;
import PennyPincher.entity.Expense;
import PennyPincher.entity.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public interface ExpenseService {
    void save(Expense expense);
    void deleteById(Integer expenseId);
    void deleteByEventId(Integer eventId);
    Expense findById(Integer expenseId);
    List<Expense> findExpensesForGivenEvent(Integer eventId);
    BigDecimal splitCostEquallyPerParticipants(BigDecimal amount, long participantsNumber);
    Map<Integer, BigDecimal> mapUserToCost(Expense expense);
    Map<Integer, BigDecimal> mapUserToPayoffAmount(Expense expense);
    Map<Integer, BigDecimal> mapUserToBalance(Expense expense);
    Expense findByExpenseNameAndEventId(String expenseName, Integer eventId);
    void updateExpenseAttributes(List<Expense> eventExpenses);
    BigDecimal calculateUpdatedBalanceForEvent(List<Expense> eventExpenses);
    Expense createExpense(Event foundEvent, User loggedInUser, CustomExpenseDto customExpenseDto, ExpenseMapper expenseMapper);
    void updateParticipantsAndDeleteExpense(Expense expense, Map<Integer, BigDecimal> costPerParticipant, Map<Integer, BigDecimal> payoffPerParticipant);
    BigDecimal calculateUserBalance(User foundUser, BigDecimal paidOffFromInput);
}
