package PennyPincher.service.payoffs;

import PennyPincher.model.Expense;
import PennyPincher.model.Payoff;
import PennyPincher.model.User;

import java.math.BigDecimal;

public interface PayoffService {
    void save(Payoff payoff);
    Payoff createPayoff(Expense foundExpense, User foundUser, BigDecimal paidOffFromInput);
}