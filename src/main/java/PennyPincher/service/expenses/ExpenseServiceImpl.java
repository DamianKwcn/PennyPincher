package PennyPincher.service.expenses;

import PennyPincher.entity.Expense;
import PennyPincher.entity.Payoff;
import PennyPincher.entity.User;
import PennyPincher.exception.ExpenseNotFoundException;
import PennyPincher.repository.ExpenseRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class ExpenseServiceImpl implements ExpenseService {

    private ExpenseRepository expenseRepository;

    @Override
    public Expense findById(Integer expenseId) {
        return expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found with ID: " + expenseId));
    }

    @Override
    public void save(Expense expense) {
        expenseRepository.save(expense);
    }

    @Override
    public void deleteByEventId(Integer eventId) {
        expenseRepository.deleteById(eventId);
    }

    @Override
    public void deleteById(Integer expenseId) {
        if (!expenseRepository.existsById(expenseId)) {
            throw new ExpenseNotFoundException("Expense not found with ID: " + expenseId);
        }
        expenseRepository.deleteById(expenseId);
    }

    @Override
    public List<Expense> findExpensesForGivenEvent(Integer eventId) {
        Optional<List<Expense>> expensesOptional = expenseRepository.findByEventId(eventId);
        return expensesOptional.orElse(Collections.emptyList());
    }

    @Override
    public Optional<Expense> findByExpenseNameAndEventId(String expenseName, Integer eventId) {
        return expenseRepository.findByNameAndEventId(expenseName, eventId);
    }

    @Override
    public BigDecimal splitCostEquallyPerParticipants(BigDecimal amount, long participantsNumber) {
        return participantsNumber == 0
                ? BigDecimal.ZERO
                : amount.divide(BigDecimal.valueOf(participantsNumber), 2, RoundingMode.CEILING);
    }

    @Override
    public Map<Integer, BigDecimal> mapUserToCost(Expense expense) {
        return expense.getParticipants().stream()
                .collect(Collectors.toMap(
                        User::getId,
                        participant -> sumParticipantCosts(expense, participant)
                ));
    }

    @Override
    public Map<Integer, BigDecimal> mapUserToPayoffAmount(Expense expense) {
        return expense.getParticipants().stream()
                .collect(Collectors.toMap(
                        User::getId,
                        participant -> sumParticipantPayoffs(expense, participant)
                ));
    }

    @Override
    public Map<Integer, BigDecimal> mapUserToBalance(Expense expense) {
        return expense.getParticipants().stream()
                .collect(Collectors.toMap(
                        User::getId,
                        participant -> calculateParticipantBalance(expense, participant)
                ));
    }

    private BigDecimal sumParticipantPayoffs(Expense expense, User participant) {
        return expense.getPayoffs().stream()
                .filter(Objects::nonNull)
                .filter(payoff -> participant.getId().equals(payoff.getUserPaying().getId()))
                .map(Payoff::getPayoffAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateParticipantBalance(Expense expense, User participant) {
        BigDecimal payoffsSum = sumParticipantPayoffs(expense, participant);
        Map<Integer, BigDecimal> costPerUser = expense.getCostPerUser();
        BigDecimal participantPayoff = costPerUser.get(participant.getId());

        if (costPerUser.containsKey(participant.getId()) && !Objects.equals(participantPayoff, BigDecimal.ZERO)) {
            return payoffsSum.subtract(participantPayoff);
        }
        return payoffsSum;
    }

    private BigDecimal sumParticipantCosts(Expense expense, User participant) {
        return expense.getCostPerUser().entrySet().stream()
                .filter(costMap -> participant.getId().equals(costMap.getKey()))
                .map(Map.Entry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
