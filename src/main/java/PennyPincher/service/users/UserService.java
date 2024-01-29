package PennyPincher.service.users;

import PennyPincher.entity.User;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface UserService {
    void save(User user);
    List<User> findAllUsers();
    User findById(Integer userId);
    User findByUsername(String username);
    List<User> findAll();
    User getCurrentlyLoggedInUser();
}
