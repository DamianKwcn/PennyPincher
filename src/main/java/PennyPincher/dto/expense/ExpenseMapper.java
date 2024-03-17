package PennyPincher.dto.expense;

import PennyPincher.entity.Event;
import PennyPincher.entity.Expense;
import PennyPincher.entity.User;
import PennyPincher.service.expenses.ExpenseService;
import PennyPincher.service.users.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ExpenseMapper {

    private final UserService userService;
    private final ExpenseService expenseService;

    public Expense mapSplitExpenseToDomain(Event event, SplitExpenseDto splitExpenseDto) {
        List<User> expenseParticipants = userService.getUsersByNames(splitExpenseDto);
        Map<Integer, BigDecimal> costPerParticipant = new HashMap<>();

        BigDecimal cost = splitExpenseDto.getCost().isBlank()
                ? BigDecimal.ZERO
                : new BigDecimal(splitExpenseDto.getCost().replaceAll(",", "."));
        BigDecimal equalPerEachParticipant = expenseService.splitCostEquallyPerParticipants(cost, expenseParticipants.size());

        expenseParticipants.forEach(participant -> {
            List<String> namesOfExistingExpenses = participant.getExpenses().stream()
                    .map(Expense::getName)
                    .collect(Collectors.toList());
            if (!namesOfExistingExpenses.contains(splitExpenseDto.getName())) {
                participant.setBalance(participant.getBalance().subtract(equalPerEachParticipant));
            }
            costPerParticipant.put(participant.getId(), equalPerEachParticipant);
            userService.save(participant);
        });

        return Expense.builder()
                .name(splitExpenseDto.getName())
                .creationDate(LocalDate.now())
                .totalCost(cost)
                .expenseBalance(cost.negate())
                .event(event)
                .participants(expenseParticipants)
                .costPerUser(costPerParticipant)
                .payoffPerUser(new HashMap<>())
                .balancePerUser(new HashMap<>())
                .build();
    }

    public Expense mapCustomExpenseDtoToDomain(Event event, CustomExpenseDto customExpenseDto) {
        List<User> expenseParticipants = userService.getUsersByNames(customExpenseDto);
        BigDecimal cost = customExpenseDto.getCost().isBlank()
                ? BigDecimal.ZERO
                : new BigDecimal(customExpenseDto.getCost().replaceAll(",", "."));

        Map<Integer, BigDecimal> userContribution = customExpenseDto.getUserContribution();

        expenseParticipants.forEach(participant -> {
            List<String> namesOfExistingExpenses = participant.getExpenses().stream()
                    .map(Expense::getName)
                    .collect(Collectors.toList());

            if (!namesOfExistingExpenses.contains(customExpenseDto.getName())) {
                BigDecimal participantContribution = userContribution.get(participant.getId());
                if (userContribution.containsKey(participant.getId())
                        && !Objects.equals(participantContribution, BigDecimal.ZERO)) {
                    participant.setBalance(participant.getBalance().subtract(participantContribution == null
                            ? BigDecimal.ZERO
                            : participantContribution));
                }
            }
            userService.save(participant);
        });

        return Expense.builder()
                .name(customExpenseDto.getName())
                .creationDate(LocalDate.now())
                .totalCost(cost)
                .expenseBalance(cost.negate())
                .event(event)
                .participants(expenseParticipants)
                .costPerUser(userContribution)
                .payoffPerUser(new HashMap<>())
                .balancePerUser(new HashMap<>())
                .build();
    }
}
