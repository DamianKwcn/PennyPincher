package PennyPincher.service.users;

import PennyPincher.dto.expense.CustomExpenseDto;
import PennyPincher.dto.expense.SplitExpenseDto;
import PennyPincher.entity.Event;
import PennyPincher.entity.Expense;
import PennyPincher.entity.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public interface UserService {
    void save(User user);
    User findById(Integer userId);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    List<User> getUsersByNames(SplitExpenseDto splitExpenseDto);
    List<User> getUsersByNames(CustomExpenseDto customExpenseDto);
    Map<Event, BigDecimal> balanceInEachEvent(User user, List<Event> events, Set<Expense> expenses);
    BigDecimal totalBalanceForUser(User loggedInUser, Map<Event, BigDecimal> balanceInEachEvent);
}

