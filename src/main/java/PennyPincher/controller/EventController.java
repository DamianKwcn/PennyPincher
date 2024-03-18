package PennyPincher.controller;

import PennyPincher.dto.event.EventDto;
import PennyPincher.dto.event.EventMapper;
import PennyPincher.entity.Event;
import PennyPincher.entity.Expense;
import PennyPincher.entity.User;
import PennyPincher.exception.EventNotFoundException;
import PennyPincher.exception.UserNotFoundException;
import PennyPincher.service.events.EventService;
import PennyPincher.service.expenses.ExpenseService;
import PennyPincher.service.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Controller
public class EventController {

    private final EventService eventService;
    private final UserService userService;
    private final ExpenseService expenseService;
    private final EventMapper eventMapper;

    @GetMapping("/events")
    public String events(@AuthenticationPrincipal UserDetails userDetails,
                         Model model) {
        String loggedInUsername = userDetails.getUsername();
        User loggedInUser = userService.findByUsername(loggedInUsername)
                .orElseThrow(() -> new UserNotFoundException("Currently logged in user not found."));

        Optional<List<Event>> eventsOptional = eventService.findEventsByUser(loggedInUser);
        List<Event> events = eventsOptional.orElseThrow(() -> new EventNotFoundException("Events not found for the user"));

        model.addAttribute("events", events);
        model.addAttribute("loggedInUserName", loggedInUsername);
        return "events";
    }

    @GetMapping("/events/{eventId}")
    public String eventDetails(@PathVariable("eventId") Integer eventId,
                               Model model) {
        Event event = eventService.findById(eventId);
        model.addAttribute("event", event);
        return "event";
    }

    @GetMapping("/newEvent")
    public String showEventAddingForm(@AuthenticationPrincipal UserDetails userDetails,
                                      Model model) {
        List<User> allUsers = userService.findAll();
        model.addAttribute("newEvent", new EventDto());
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("loggedInUserName", userDetails.getUsername());
        return "new-event";
    }

    @PostMapping("/newEvent")
    public String createEvent(@AuthenticationPrincipal UserDetails userDetails,
                              @ModelAttribute("newEvent") EventDto eventDto,
                              BindingResult result) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        validateEventNotExist(eventDto.getEventName(), user, result);
        validateEventNameNotBlank(eventDto.getEventName(), result);

        if (result.hasErrors()) {
            return "new-event";
        }

        eventService.save(eventMapper.mapToDomain(eventDto, userDetails));
        return "redirect:/events";
    }

    @DeleteMapping("/deleteEvent/{eventId}")
    public String deleteEvent(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Integer eventId,
                              RedirectAttributes redirectAttributes) {
        userService.findByUsername(userDetails.getUsername()).ifPresent(user -> {
            Event event = eventService.findById(eventId);

            if (event.getEventBalance() != null && event.getEventBalance().compareTo(BigDecimal.ZERO) < 0) {
                redirectAttributes.addFlashAttribute("deleteError", "You cannot delete an unsettled event.");
            } else if (event.getOwner().equals(user)) {
                expenseService.deleteByEventId(eventId);
                eventService.deleteById(eventId);
            }
        });
        return "redirect:/events";
    }

    @GetMapping("/events/{eventId}/users")
    public String showEventUsers(@AuthenticationPrincipal UserDetails userDetails,
                                 @PathVariable("eventId") Integer eventId,
                                 Model model) {
        User loggedInUser = getLoggedInUser(userDetails);
        Event event = eventService.findById(eventId);
        List<User> allUsers = userService.findAll();
        List<User> eventMembers = event.getEventMembers();
        List<User> remainingUsers = new ArrayList<>();
        List<Expense> eventExpenses = expenseService.findExpensesForGivenEvent(eventId);

        eventService.populateUserLists(allUsers, eventMembers, remainingUsers);

        model.addAttribute("event", event);
        saveCommonAttributes(model, eventId, eventMembers, remainingUsers, eventExpenses, loggedInUser);
        return "users";
    }

    @GetMapping("/events/{eventId}/expenses")
    public String showEventExpenses(@AuthenticationPrincipal UserDetails userDetails,
                                    @PathVariable("eventId") Integer eventId,
                                    @RequestParam(value = "errorMessage", required = false) String errorMessage,
                                    Model model) {
        User loggedInUser = getLoggedInUser(userDetails);
        Event event = eventService.findById(eventId);
        List<User> allUsers = userService.findAll();
        List<User> eventMembers = event.getEventMembers();
        List<User> remainingUsers = new ArrayList<>();
        List<Expense> eventExpenses = expenseService.findExpensesForGivenEvent(eventId);

        expenseService.updateExpenseAttributes(eventExpenses);
        eventService.populateUserLists(allUsers, eventMembers, remainingUsers);
        BigDecimal updatedBalance = expenseService.calculateUpdatedBalanceForEvent(eventExpenses);
        event.setEventBalance(updatedBalance);
        eventService.save(event);

        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
        }

        model.addAttribute("user", loggedInUser);
        model.addAttribute("event", event);
        saveCommonAttributes(model, eventId, eventMembers, remainingUsers, eventExpenses, loggedInUser);
        model.addAttribute("updatedBalance", updatedBalance);
        return "expenses";
    }

    @PostMapping("/events/{eventId}/addUser")
    public String addUser(@PathVariable("eventId") Integer eventId,
                          @RequestParam("userId") Integer userId) {
        Event event = eventService.findById(eventId);
        User user = userService.findById(userId);
        event.addEventMember(user);
        eventService.save(event);
        user.addEvent(event);
        userService.save(user);
        return "redirect:/events/" + eventId + "/users";
    }

    @DeleteMapping("/events/{eventId}/removeUser")
    public String removeUser(@PathVariable("eventId") Integer eventId,
                             @RequestParam("userId") Integer userId) {
        Event event = eventService.findById(eventId);
        User user = userService.findById(userId);
        event.removeEventMember(user);
        eventService.save(event);
        user.removeEvent(event);
        userService.save(user);
        return "redirect:/events/" + eventId + "/users";
    }

    @PostMapping("/events/{eventId}/setAsEventOwner/{userId}")
    public String setAsEventOwner(@PathVariable("eventId") Integer eventId,
                                  @PathVariable("userId") Integer userId,
                                  Model model) {
        Event event = eventService.findById(eventId);
        User user = userService.findById(userId);
        event.setOwner(user);
        eventService.save(event);
        model.addAttribute("loggedInUserName", user.getUsername());
        return "redirect:/events";
    }

    private void validateEventNotExist(String eventName,
                                       User user,
                                       BindingResult result) {
        Optional<Event> existingEvent = eventService.findByEventNameAndOwner(eventName, user);

        if (existingEvent.isPresent()) {
            result.addError(new FieldError("newEvent", "eventName",
                    "You already have an event with the name '" + eventName + "'. Please choose a different name."));
        }
    }

    private void validateEventNameNotBlank(String eventName,
                                           BindingResult result) {
        if (eventName.isBlank()) {
            result.addError(new FieldError("newEvent", "eventName",
                    "Event name field cannot be blank."));
        }
    }

    private User getLoggedInUser(UserDetails userDetails) {
        return userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + userDetails.getUsername()));
    }

    private void saveCommonAttributes(Model model,
                                      Integer eventId,
                                      List<User> eventMembers,
                                      List<User> remainingUsers,
                                      List<Expense> eventExpenses,
                                      User loggedInUser) {
        model.addAttribute("add_id", eventId);
        model.addAttribute("remove_id", eventId);
        model.addAttribute("eventMembers", eventMembers);
        model.addAttribute("remainingUsers", remainingUsers);
        model.addAttribute("eventExpenses", eventExpenses);
        model.addAttribute("loggedInUserName", loggedInUser.getUsername());
    }
}
