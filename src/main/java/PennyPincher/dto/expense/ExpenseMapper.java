package PennyPincher.dto.expense;

import PennyPincher.entity.Event;
import PennyPincher.entity.Expense;
import PennyPincher.entity.User;
import PennyPincher.service.expenses.ExpenseService;
import PennyPincher.service.users.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ExpenseMapper {

    private final UserService userService;
    private final ExpenseService expenseService;

    public Expense mapToDomain(Event event, ExpenseDto expenseDto) {
        TreeSet<User> expenseParticipants = userService.getUsersByNames(expenseDto);
        BigDecimal cost = expenseDto.getCost().isBlank()
                ? BigDecimal.ZERO
                : new BigDecimal(expenseDto.getCost().replaceAll(",", "."));
        BigDecimal costPerParticipant = expenseService.splitCostEquallyPerParticipants(cost, expenseParticipants.size());

        expenseParticipants.forEach(participant -> {
            List<String> namesOfExistingExpenses = participant.getExpenses().stream()
                    .map(Expense::getName)
                    .collect(Collectors.toList());
            if (!namesOfExistingExpenses.contains(expenseDto.getName())) {
                participant.setBalance(userService.calculateUserBalance(participant.getId()).subtract(costPerParticipant));
            }
            userService.save(participant);
        });

        return Expense.builder()
                .name(expenseDto.getName())
                .totalCost(cost)
                .costPerParticipant(costPerParticipant)
                .event(event)
                .participants(expenseParticipants)
                .payoffAmountPerUser(new HashMap<>())
                .balancePerUser(new HashMap<>())
                .build();
    }
}
