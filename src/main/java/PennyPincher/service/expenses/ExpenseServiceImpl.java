package PennyPincher.service.expenses;

import PennyPincher.dto.expense.CustomExpenseDto;
import PennyPincher.dto.expense.ExpenseMapper;
import PennyPincher.entity.Event;
import PennyPincher.entity.Expense;
import PennyPincher.entity.Payoff;
import PennyPincher.entity.User;
import PennyPincher.exception.ExpenseNotFoundException;
import PennyPincher.repository.ExpenseRepository;
import PennyPincher.service.users.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserService userService;

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
        return expenseRepository.findByEventId(eventId);
    }

    @Override
    public Expense findByExpenseNameAndEventId(String expenseName, Integer eventId) {
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

    @Override
    public void updateExpenseAttributes(List<Expense> eventExpenses) {
        for (Expense expense : eventExpenses) {
            Map<Integer, BigDecimal> payoffAmountPerParticipant = mapUserToPayoffAmount(expense);
            Map<Integer, BigDecimal> balancePerParticipant = mapUserToBalance(expense);
            Map<Integer, BigDecimal> costPerParticipant = mapUserToCost(expense);
            expense.setCostPerUser(costPerParticipant);
            expense.setPayoffPerUser(payoffAmountPerParticipant);
            expense.setBalancePerUser(balancePerParticipant);
            save(expense);
        }
    }

    @Override
    public BigDecimal calculateUpdatedBalanceForEvent(List<Expense> eventExpenses) {
        return eventExpenses.stream()
                .flatMap(expense -> expense.getBalancePerUser().values().stream())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Expense createExpense(Event foundEvent,
                                 User loggedInUser,
                                 CustomExpenseDto customExpenseDto,
                                 ExpenseMapper expenseMapper) {
        List<User> eventMembers = foundEvent.getEventMembers();
        List<String> namesOfMembers = eventMembers.stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
        customExpenseDto.setParticipantsNames(namesOfMembers);

        Expense expense = expenseMapper.mapCustomExpenseDtoToDomain(foundEvent, customExpenseDto);

        List<Payoff> expensePayoffs = new ArrayList<>();
        Payoff defaultPayoff = Payoff.builder()
                .expensePaid(expense)
                .userPaying(loggedInUser)
                .payoffAmount(BigDecimal.ZERO)
                .build();
        expensePayoffs.add(defaultPayoff);
        expense.setPayoffs(expensePayoffs);

        return expense;
    }

    @Override
    public void updateParticipantsAndDeleteExpense(Expense expense,
                                                   Map<Integer, BigDecimal> costPerParticipant,
                                                   Map<Integer, BigDecimal> payoffPerParticipant) {
        expense.getParticipants().forEach(participant -> {
            BigDecimal participantBalanceChange = costPerParticipant.getOrDefault(participant.getId(), BigDecimal.ZERO)
                    .subtract(payoffPerParticipant.getOrDefault(participant.getId(), BigDecimal.ZERO));
            participant.setBalance(participant.getBalance().add(participantBalanceChange));
            participant.getExpenses().removeIf(exp -> exp.getId().equals(expense.getId()));
            userService.save(participant);
        });
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
