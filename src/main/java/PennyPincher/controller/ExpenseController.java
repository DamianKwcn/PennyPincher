package PennyPincher.controller;

import PennyPincher.dto.expense.CustomExpenseDto;
import PennyPincher.dto.expense.SplitExpenseDto;
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
import java.util.stream.Collectors;

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

    private final Pattern costPattern = Pattern.compile("[0-9]+(\\.[0-9]{1,2})?");

    @GetMapping("/events/{eventId}/newSplitExpense")
    public String showSplitExpenseForm(@PathVariable Integer eventId,
                                       Model model) {
        Event event = eventService.findById(eventId);
        List<User> eventMembers = event.getEventMembers();

        model.addAttribute("event", event);
        model.addAttribute("eventMembers", eventMembers);
        model.addAttribute("newSplitExpense", new SplitExpenseDto());
        return "new-split-expense";
    }

    @GetMapping("/events/{eventId}/newCustomExpense")
    public String showCustomExpenseForm(@PathVariable Integer eventId,
                                        Model model) {
        Event event = eventService.findById(eventId);
        List<User> eventMembers = event.getEventMembers();

        model.addAttribute("event", event);
        model.addAttribute("eventMembers", eventMembers);
        model.addAttribute("newCustomExpense", new CustomExpenseDto());
        model.addAttribute("loggedInUserName", userService.getCurrentlyLoggedInUser().getUsername());
        return "new-custom-expense";
    }

    @PostMapping("/events/{eventId}/saveSplitExpense")
    public String createSplitExpense(@ModelAttribute("newSplitExpense") SplitExpenseDto splitExpenseDto,
                                     @PathVariable Integer eventId,
                                     BindingResult result,
                                     Model model) {
        Event foundEvent = eventService.findById(eventId);
        validateSplitExpense(eventId, splitExpenseDto, result);
        saveAttributesToSplitModel(eventId, splitExpenseDto, model);

        if (result.hasErrors()) {
            return "new-split-expense";
        }

        Expense expense = expenseMapper.mapSplitExpenseToDomain(foundEvent, splitExpenseDto);
        expenseService.save(expense);
        return "redirect:/events/" + eventId + "/expenses";
    }

    @PostMapping("/events/{eventId}/saveCustomExpense")
    public String createCustomExpense(@ModelAttribute("newCustomExpense") CustomExpenseDto customExpenseDto,
                                      @PathVariable Integer eventId,
                                      BindingResult result,
                                      Model model) {
        Event foundEvent = eventService.findById(eventId);
        User loggedInUser = userService.getCurrentlyLoggedInUser();
        List<User> eventMembers = foundEvent.getEventMembers();
        validateCustomExpense(eventId, customExpenseDto, result);
        saveAttributesToCustomModel(eventId, customExpenseDto, model);

        if (result.hasErrors()) {
            return "new-custom-expense";
        }

        List<String> namesOfMembers = eventMembers.stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
        customExpenseDto.setParticipantsNames(namesOfMembers);
        Expense expense = expenseMapper.mapCustomExpenseDtoToDomain(foundEvent, customExpenseDto);

        List<Payoff> expensePayoffs = new ArrayList<>();
        Payoff defaultPayoff = Payoff.builder()
                .expensePaid(expense)
                .userPaying(loggedInUser)
                .payoffAmount(BigDecimal.ZERO)
                .build();
        expensePayoffs.add(defaultPayoff);
        expense.setPayoffs(expensePayoffs);

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

        String errorMessage = null;

        BigDecimal paidOffFromInput = (paidOffAmount == null || paidOffAmount.isEmpty())
                ? BigDecimal.ZERO.setScale(2, RoundingMode.CEILING)
                : new BigDecimal(paidOffAmount.replaceAll(",", ".")).setScale(2, RoundingMode.CEILING);
        BigDecimal userBalance = foundUser.getBalance().add(paidOffFromInput);

        Payoff payoff = Payoff.builder()
                .expensePaid(foundExpense)
                .userPaying(foundUser)
                .payoffAmount(paidOffFromInput)
                .build();

        if (foundExpense.getTotalCost() != null) {
            foundExpense.setExpenseBalance(foundExpense.getExpenseBalance().add(paidOffFromInput));
        }
        foundExpense.getPayoffs().add(payoff);
        foundUser.getPayoffs().add(payoff);
        foundUser.setBalance(userBalance);

        if (userBalance.compareTo(BigDecimal.ZERO) > 0) {
            errorMessage = "Too big amount of paid off";
        }
        if (errorMessage != null) {
            return "redirect:/events/" + eventId + "/expenses?errorMessage=" + errorMessage;
        }

        userService.save(foundUser);
        expenseService.save(foundExpense);
        payoffService.save(payoff);

        model.addAttribute("paidOffAmount", paidOffAmount);
        model.addAttribute("userBalance", userBalance);
        return "redirect:/events/" + eventId + "/expenses";
    }

    @GetMapping("/events/{eventId}/expenses/{expenseId}/delete")
    public String deleteExpense(@PathVariable Integer eventId,
                                @PathVariable Integer expenseId,
                                Model model) {
        Event foundEvent = eventService.findById(eventId);
        Expense foundExpense = expenseService.findById(expenseId);
        Map<Integer, BigDecimal> costPerParticipant = foundExpense.getCostPerUser();
        Map<Integer, BigDecimal> payoffPerParticipant = foundExpense.getPayoffPerUser();

        foundExpense.getParticipants().forEach(participant -> {
            BigDecimal participantBalanceChange = costPerParticipant.getOrDefault(participant.getId(), BigDecimal.ZERO)
                    .subtract(payoffPerParticipant.getOrDefault(participant.getId(), BigDecimal.ZERO));
            participant.setBalance(participant.getBalance().add(participantBalanceChange));
            participant.getExpenses().removeIf(expense -> expense.getId().equals(expenseId));
            userService.save(participant);
        });

        foundEvent.removeExpense(foundExpense);
        expenseService.deleteById(expenseId);
        model.addAttribute("loggedInUserName", userService.getCurrentlyLoggedInUser().getUsername());
        return "redirect:/events/" + eventId + "/expenses";
    }

    private void validateSplitExpense(Integer eventId,
                                      SplitExpenseDto splitExpenseDto,
                                      BindingResult result) {
        Matcher matcher = costPattern.matcher(splitExpenseDto.getCost());

        Optional<Expense> existingExpense =
                Optional.ofNullable(expenseService.findByExpenseNameAndEventId(splitExpenseDto.getName(), eventId));
        existingExpense.ifPresent(expense -> result.addError(new FieldError("newExpense", "name",
                "Expense '" + expense.getName() + "' already exists.")));

        if (splitExpenseDto.getName().isBlank()) {
            result.addError(new FieldError("newExpense", "name",
                    "Expense name field cannot be empty."));
        }
        if (!matcher.matches()) {
            result.addError(new FieldError("newExpense", "cost",
                    "Enter proper value for expense amount."));
        }
        if (splitExpenseDto.getCost().isEmpty()) {
            result.addError(new FieldError("newExpense", "cost",
                    "Expense amount field cannot be empty."));
        }
    }

    private void validateCustomExpense(Integer eventId,
                                       CustomExpenseDto customExpenseDto,
                                       BindingResult result) {
        Matcher matcher = costPattern.matcher(customExpenseDto.getCost());

        Optional<Expense> existingExpense = Optional.ofNullable(expenseService.findByExpenseNameAndEventId(customExpenseDto.getName(), eventId));
        existingExpense.ifPresent(expense -> result.addError(new FieldError("newExpense", "name",
                "Expense '" + expense.getName() + "' already exists.")));

        if (customExpenseDto.getName().isBlank()) {
            result.addError(new FieldError("newExpense", "name",
                    "Expense name field cannot be empty."));
        }

        if (customExpenseDto.getCost().isEmpty()) {
            result.addError(new FieldError("newExpense", "cost",
                    "Expense amount field cannot be empty."));
        }

        if (!matcher.matches()) {
            result.addError(new FieldError("newExpense", "cost",
                    "Enter proper value for expense amount."));
        }
    }

    private void saveAttributesToSplitModel(Integer eventId,
                                            SplitExpenseDto splitExpenseDto,
                                            Model model) {
        Event foundEvent = eventService.findById(eventId);
        User loggedInUser = userService.getCurrentlyLoggedInUser();
        List<User> eventMembers = foundEvent.getEventMembers();
        List<User> expenseParticipants = userService.getUsersByNames(splitExpenseDto);

        model.addAttribute("event", foundEvent);
        model.addAttribute("loggedInUserName", loggedInUser.getUsername());
        model.addAttribute("eventMembers", eventMembers);
        model.addAttribute("expenseParticipants", expenseParticipants);
    }

    private void saveAttributesToCustomModel(Integer eventId,
                                             CustomExpenseDto customExpenseDto,
                                             Model model) {
        Event foundEvent = eventService.findById(eventId);
        User loggedInUser = userService.getCurrentlyLoggedInUser();
        List<User> eventMembers = foundEvent.getEventMembers();
        List<User> expenseParticipants = userService.getUsersByNames(customExpenseDto);

        model.addAttribute("event", foundEvent);
        model.addAttribute("loggedInUserName", loggedInUser.getUsername());
        model.addAttribute("eventMembers", eventMembers);
        model.addAttribute("expenseParticipants", expenseParticipants);
    }
}

