package PennyPincher.service.payoffs;

import PennyPincher.entity.Expense;
import PennyPincher.entity.Payoff;
import PennyPincher.entity.User;

import java.math.BigDecimal;

public interface PayoffService {
    void save(Payoff payoff);
    Payoff createPayoff(Expense foundExpense, User foundUser, BigDecimal paidOffFromInput);
}