package PennyPincher.service.users;

import PennyPincher.dto.expense.CustomExpenseDto;
import PennyPincher.dto.expense.SplitExpenseDto;
import PennyPincher.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    void save(User user);
    User findById(Integer userId);
    User findByUsername(String username);
    List<User> findAll();
    User getCurrentlyLoggedInUser();
    List<User> getUsersByNames(SplitExpenseDto splitExpenseDto);
    List<User> getUsersByNames(CustomExpenseDto customExpenseDto);
}

