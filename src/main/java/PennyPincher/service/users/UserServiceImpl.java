package PennyPincher.service.users;

import PennyPincher.dto.expense.CustomExpenseDto;
import PennyPincher.dto.expense.SplitExpenseDto;
import PennyPincher.model.Event;
import PennyPincher.model.Expense;
import PennyPincher.model.User;
import PennyPincher.model.UsernameComparator;
import PennyPincher.exception.UserNotFoundException;
import PennyPincher.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User findById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    @Override
    public Optional<User> findByUsername(String username) {
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
    public BigDecimal totalBalanceForUser(User loggedInUser, Map<Event, BigDecimal> balanceInEachEvent) {
        return balanceInEachEvent.entrySet().stream()
                .filter(entry -> entry.getKey().getEventMembers().contains(loggedInUser))
                .map(Map.Entry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Map<Event, BigDecimal> balanceInEachEvent(User user, List<Event> events, Set<Expense> expenses) {
        Map<Event, BigDecimal> balanceMap = new HashMap<>();

        for (Event event : events) {
            BigDecimal userBalanceInEvent = expenses.stream()
                    .filter(expense -> expense.getEvent().equals(event))
                    .filter(expense -> expense.getParticipants().contains(user))
                    .map(expense -> {
                        BigDecimal userAmount = expense.getCostPerUser().getOrDefault(user.getId(), BigDecimal.ZERO);
                        BigDecimal userPayoff = expense.getPayoffPerUser().getOrDefault(user.getId(), BigDecimal.ZERO);
                        return userAmount.subtract(userPayoff);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::subtract);
            balanceMap.put(event, userBalanceInEvent);
        }

        return balanceMap;
    }

    @Override
    public List<User> getUsersByNames(SplitExpenseDto splitExpenseDto) {
        if (splitExpenseDto.getParticipantUsername() != null) {
            return Arrays.stream(splitExpenseDto.getParticipantUsername().split(","))
                    .map(this::findByUsername)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .sorted(new UsernameComparator())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<User> getUsersByNames(CustomExpenseDto customExpenseDto) {
        if (customExpenseDto.getParticipantsNames() != null) {
            return customExpenseDto.getParticipantsNames()
                    .stream()
                    .map(this::findByUsername)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .distinct()
                    .sorted(new UsernameComparator())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}