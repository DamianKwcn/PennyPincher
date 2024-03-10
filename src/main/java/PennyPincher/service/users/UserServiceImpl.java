package PennyPincher.service.users;

import PennyPincher.dto.expense.CustomExpenseDto;
import PennyPincher.dto.expense.SplitExpenseDto;
import PennyPincher.entity.Event;
import PennyPincher.entity.Expense;
import PennyPincher.entity.User;
import PennyPincher.entity.UsernameComparator;
import PennyPincher.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        TreeSet<User> participants = new TreeSet<User>(new UsernameComparator());
        if (splitExpenseDto.getParticipantUsername() != null) {
            String[] splitUsernames = splitExpenseDto.getParticipantUsername().split("[,]", 0);
            for (String username : splitUsernames) {
                User foundUser = this.findByUsername(username);
                participants.add(foundUser);
            }
        }
        return participants.stream().sorted(new UsernameComparator()).collect(Collectors.toList());
    }

    @Override
    public List<User> getUsersByNames(CustomExpenseDto customExpenseDto) {
        TreeSet<User> participants = new TreeSet<User>(new UsernameComparator());
        if (customExpenseDto.getParticipantsNames() != null) {
            Set<User> retrievedUsers = customExpenseDto.getParticipantsNames().stream()
                    .map(this::findByUsername)
                    .collect(Collectors.toSet());
            participants.addAll(retrievedUsers);
        }
        return participants.stream().sorted(new UsernameComparator()).collect(Collectors.toList());
    }

}