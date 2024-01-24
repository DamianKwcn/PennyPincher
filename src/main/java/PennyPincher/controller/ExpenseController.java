package PennyPincher.controller;

import PennyPincher.dto.expense.ExpenseDto;
import PennyPincher.dto.expense.ExpenseMapper;
import PennyPincher.entity.Event;
import PennyPincher.entity.Expense;
import PennyPincher.entity.Payoff;
import PennyPincher.entity.User;
import PennyPincher.repository.EventRepository;
import PennyPincher.repository.ExpenseRepository;
import PennyPincher.repository.UserRepository;
import PennyPincher.service.events.EventService;
import PennyPincher.service.expenses.ExpenseService;
import PennyPincher.service.payoffs.PayoffService;
import PennyPincher.service.users.UserService;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Controller
public class ExpenseController {
        private final EventService eventService;
        private final EventRepository eventRepository;
        private final UserService userService;
        private final UserRepository userRepository;
        private final ExpenseService expenseService;
        private final ExpenseRepository expenseRepository;
        private final PayoffService payoffService;
        private final ExpenseMapper expenseMapper;

        @GetMapping("/events/{eventId}/newExpense")
        public String showExpenseForm(@PathVariable Integer eventId, Model model) {
            Event event = eventService.findById(eventId);
            List<User> eventMembers = event.getEventMembers();

            model.addAttribute("event", event);
            model.addAttribute("eventMembers", eventMembers);
            model.addAttribute("newExpense", new ExpenseDto());
            model.addAttribute("loggedInUserName", userService.getCurrentlyLoggedInUser().getUsername());
            return "new-expense";
        }

        @GetMapping("/events/{eventId}/expenses/{expenseId}/delete")
        public String deleteExpense(@PathVariable Integer eventId,
                                    @PathVariable Integer expenseId,
                                    Model model) {
            Event foundEvent = eventService.findById(eventId);
            Expense foundExpense = expenseService.findById(expenseId);
            BigDecimal costPerParticipant = foundExpense.getCostPerParticipant();

            foundExpense.getParticipants().forEach(participant -> {
                participant.setBalance(userService.calculateUserBalance(participant.getId()).add(costPerParticipant));
                participant.getExpenses().removeIf(expense -> participant.getExpenses().contains(expense));
                userService.save(participant);
            });

            foundEvent.removeExpense(foundExpense);
            expenseService.deleteById(expenseId);
            model.addAttribute("loggedInUserName", userService.getCurrentlyLoggedInUser().getUsername());
            return "redirect:/events/" + eventId + "/expenses";
        }

        @PostMapping("/events/{eventId}/saveExpense")
        public String createExpense(@ModelAttribute("newExpense") ExpenseDto expenseDto,
                                    @PathVariable Integer eventId,
                                    BindingResult result,
                                    Model model) {
            Event foundEvent = eventService.findById(eventId);
            model.addAttribute("event", foundEvent);

            User loggedInUser = userService.getCurrentlyLoggedInUser();
            model.addAttribute("loggedInUserName", loggedInUser.getUsername());

            List<User> eventMembers = foundEvent.getEventMembers();
            model.addAttribute("eventMembers", eventMembers);

            Optional<Expense> existingExpense = Optional.ofNullable(expenseService.findByExpenseNameAndEventId(expenseDto.getName(), eventId));
            existingExpense.ifPresent(expense -> result.addError(new FieldError("newExpense", "name",
                    "Expense '" + expense.getName() + "' already exists.")));

            if (expenseDto.getName().isBlank()) {
                result.addError(new FieldError("newExpense", "name",
                        "Expense name field cannot be empty."));
            }

            Pattern pattern = Pattern.compile("[0-9]+(\\.[0-9]{1,2})?");
            Matcher matcher = pattern.matcher(expenseDto.getCost());

            if (!matcher.matches()) {
                result.addError(new FieldError("newExpense", "cost",
                        "Enter proper value for expense amount."));
            }
            if (expenseDto.getCost().isEmpty()) {
                result.addError(new FieldError("newExpense", "cost",
                        "Expense amount field cannot be empty."));
            }

            if (result.hasErrors()) {
                return "new-expense";
            }
            Expense expense = expenseMapper.mapToDomain(foundEvent, expenseDto);
            TreeSet<User> expenseParticipants = userService.getUsersByNames(expenseDto);
            List<Payoff> expensePayoffs = new ArrayList<>();

            Payoff defaultPayoff = Payoff.builder()
                    .expensePaid(expense)
                    .userPaying(loggedInUser)
                    .payoffAmount(BigDecimal.ZERO)
                    .build();
            expensePayoffs.add(defaultPayoff);
            expense.setPayoffs(expensePayoffs);

            model.addAttribute("expenseParticipants", expenseParticipants);

            expenseService.save(expense);

            return "redirect:/events/" + eventId + "/expenses";
        }

        @GetMapping("/events/{eventId}/expenses/{expenseId}/users/{userId}")
        public String assignPaidOffAmount(@PathVariable("eventId") Integer eventId,
                                          @PathVariable("expenseId") Integer expenseId,
                                          @PathVariable("userId") Integer userId,
                                          @RequestParam("paidOffAmount") String paidOffAmount,
                                          Model model) {
            Expense foundExpense = expenseService.findById(expenseId);
            User foundUser = userService.findById(userId);

            model.addAttribute("paidOffAmount", paidOffAmount);

            BigDecimal paidOffFromInput = paidOffAmount == null
                    ? BigDecimal.ZERO.setScale(2, RoundingMode.CEILING)
                    : new BigDecimal(paidOffAmount.replaceAll(",", ".")).setScale(2, RoundingMode.CEILING);
            BigDecimal userBalance = userService.calculateUserBalance(foundUser.getId()).add(paidOffFromInput);

            Payoff payoff = Payoff.builder()
                    .expensePaid(foundExpense)
                    .userPaying(foundUser)
                    .payoffAmount(paidOffFromInput)
                    .build();

            if (foundExpense.getTotalCost() != null) {
                foundExpense.setExpenseBalance(paidOffFromInput.subtract(foundExpense.getTotalCost()));
            }
            foundExpense.getPayoffs().add(payoff);
            foundUser.getPayoffs().add(payoff);
            foundUser.setBalance(userBalance);

            userService.save(foundUser);
            expenseService.save(foundExpense);
            payoffService.save(payoff);

            model.addAttribute("userBalance", userBalance);

            return "redirect:/events/" + eventId + "/expenses";
        }

        @GetMapping("/expenses/{expenseId}/addUser")
        public String addUser(@PathVariable("expenseId") Integer expenseId, @RequestParam("userId") Integer userId) {
            Expense expense = expenseService.findById(expenseId);
            User user = userService.findById(userId);
            expense.addParticipant(user);
            expenseService.save(expense);
            user.addExpense(expense);
            userService.save(user);
            return "redirect:/expenses/" + expenseId + "/users";
        }

        @GetMapping("/expenses/{expenseId}/removeUser")
        public String removeUser(@PathVariable("expenseId") Integer expenseId, @RequestParam("userId") Integer userId) {
            Expense expense = expenseService.findById(expenseId);
            User user = userService.findById(userId);
            expense.removeParticipant(user);
            expenseService.save(expense);
            user.removeExpense(expense);
            userService.save(user);
            return "redirect:/expenses/" + expenseId + "/users";
        }
}

