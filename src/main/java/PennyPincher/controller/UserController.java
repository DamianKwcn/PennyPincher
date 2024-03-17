package PennyPincher.controller;

import PennyPincher.dto.user.UserDto;
import PennyPincher.dto.user.UserMapper;
import PennyPincher.entity.Event;
import PennyPincher.entity.Expense;
import PennyPincher.entity.User;
import PennyPincher.exception.UserNotFoundException;
import PennyPincher.service.users.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.util.*;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserDto());
        return "register";
    }

    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") UserDto userDto,
                               BindingResult result,
                               Model model) {

        if (doesUserAlreadyExist(userDto.getUsername())) {
            result.rejectValue("username", null,
                    "There is already an account registered with the same username");
        }
        if (result.hasErrors()) {
            model.addAttribute("user", userDto);
            return "/register";
        }

        userService.save(userMapper.mapToDomain(userDto));
        return "redirect:/register?success";
    }

    @GetMapping("/profile")
    public String userProfile(@AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        String username = userDetails.getUsername();
        Optional<User> optionalUser = userService.findByUsername(username);

        if (optionalUser.isPresent()) {
            User loggedInUser = optionalUser.get();
            saveAttributesToUserModel(loggedInUser, model);
            return "profile";
        } else {
            throw new UserNotFoundException("Currently logged in user not found.");
        }
    }

    private boolean doesUserAlreadyExist(String userName) {
        if (userName == null || userName.isBlank()) {
            return false;
        }
        Optional<User> optionalUser = userService.findByUsername(userName);
        return optionalUser.isPresent();
    }

    private void saveAttributesToUserModel(User loggedInUser,
                                           Model model) {
        List<Event> userEvents = loggedInUser.getUserEvents();
        Set<Expense> expenses = loggedInUser.getExpenses();

        userEvents.removeIf(event -> event.getEventBalance() == null);
        userEvents.sort(Comparator.comparing(Event::getEventBalance));

        Map<Event, BigDecimal> balanceInEachEvent = userService.balanceInEachEvent(loggedInUser, userEvents, expenses);
        BigDecimal totalBalanceForUser = userService.totalBalanceForUser(loggedInUser, balanceInEachEvent);

        model.addAttribute("userEvents", userEvents);
        model.addAttribute("balanceInEachEvent", balanceInEachEvent);
        model.addAttribute("loggedInUserName", loggedInUser.getUsername());
        model.addAttribute("userBalance", totalBalanceForUser);
    }
}
