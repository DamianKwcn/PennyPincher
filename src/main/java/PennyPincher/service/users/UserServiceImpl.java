package PennyPincher.service.users;

import PennyPincher.dto.expense.ExpenseDto;
import PennyPincher.entity.Event;
import PennyPincher.entity.Expense;
import PennyPincher.entity.User;
import PennyPincher.entity.UsernameComparator;
import PennyPincher.repository.UserRepository;
import PennyPincher.service.events.EventService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final EventService eventService;
    private final UserRepository userRepository;

    @Override
    public User findById(Integer userId) {
        return userRepository.findById(userId).orElseThrow();
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public User getCurrentlyLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username);
    }

    @Override
    public BigDecimal calculateUserBalance(Integer userId) {
        User foundUser = userRepository.findById(userId).orElseThrow();

        List<Event> eventsWithUserAsParticipant = eventService.findAllEvents().stream()
                .filter(event -> event.getEventMembers().contains(foundUser))
                .collect(Collectors.toList());

        List<Expense> expensesWithUserAsParticipant = eventsWithUserAsParticipant.stream()
                .map(Event::getExpenses)
                .flatMap(List::stream)
                .filter(expense -> expense.getParticipants().contains(foundUser))
                .collect(Collectors.toList());

        return expensesWithUserAsParticipant.stream()
                .map(Expense::getCostPerParticipant)
                .reduce(BigDecimal.ZERO, BigDecimal::subtract);
    }

    @Override
    public TreeSet<User> getUsersByNames(ExpenseDto expenseDto) {
        TreeSet<User> participants = new TreeSet<User>(new UsernameComparator());
        if (expenseDto.getParticipantUsername() != null) {
            String[] splitUsernames = expenseDto.getParticipantUsername().split("[,]", 0);
            for (String username : splitUsernames) {
                User foundUser = this.findByUsername(username);
                participants.add(foundUser);
            }
        }
        return participants;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

}