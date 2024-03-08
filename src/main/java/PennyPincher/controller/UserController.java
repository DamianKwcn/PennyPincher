package PennyPincher.controller;

import PennyPincher.entity.Event;
import PennyPincher.service.users.UserService;
import PennyPincher.dto.user.UserDto;
import PennyPincher.dto.user.UserMapper;
import PennyPincher.entity.User;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Comparator;
import java.util.List;

@Data
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
    public String userProfile(Model model) {
        User loggedInUser = userService.getCurrentlyLoggedInUser();
        List<Event> userEvents = loggedInUser.getUserEvents();

        userEvents.removeIf(event -> event.getEventBalance() == null);
        userEvents.sort(Comparator.comparing(Event::getEventBalance));

        model.addAttribute("userEvents", userEvents);
        model.addAttribute("loggedInUserName", loggedInUser.getUsername());
        model.addAttribute("userBalance", loggedInUser.getBalance());
        return "profile";
    }

    private boolean doesUserAlreadyExist(String userName) {
        User foundUser = userService.findByUsername(userName);
        return foundUser != null && !foundUser.getUsername().isBlank();
    }
}
