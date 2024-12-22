package PennyPincher.service.payoffs;

import PennyPincher.model.Expense;
import PennyPincher.model.Payoff;
import PennyPincher.model.User;
import PennyPincher.repository.PayoffRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class PayoffServiceImpl implements PayoffService {

    private final PayoffRepository payoffRepository;

    @Override
    public void save(Payoff payoff) {
        payoffRepository.save(payoff);
    }

    @Override
    public Payoff createPayoff(Expense foundExpense, User foundUser, BigDecimal paidOffFromInput) {
        Payoff payoff = Payoff.builder()
                .expensePaid(foundExpense)
                .userPaying(foundUser)
                .payoffAmount(paidOffFromInput)
                .build();

        if (foundExpense.getTotalCost() != null) {
            foundExpense.setExpenseBalance(foundExpense.getExpenseBalance().add(paidOffFromInput));
        }
        foundExpense.getPayoffs().add(payoff);
        foundUser.getPayoffs().add(payoff);

        return payoff;
    }

}
