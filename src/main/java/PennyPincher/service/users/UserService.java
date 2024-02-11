package PennyPincher.service.users;

import PennyPincher.dto.expense.ExpenseDto;
import PennyPincher.entity.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.TreeSet;

@Service
public interface UserService {
    void save(User user);
    List<User> findAllUsers();
    User findById(Integer userId);
    User findByUsername(String username);
    List<User> findAll();
    User getCurrentlyLoggedInUser();
    BigDecimal calculateUserBalance(Integer userId);
    TreeSet<User> getUsersByNames(ExpenseDto expenseDto);
}

