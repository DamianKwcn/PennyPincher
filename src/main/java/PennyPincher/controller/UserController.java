package PennyPincher.controller;

import PennyPincher.service.users.UserService;
import PennyPincher.dto.user.UserDto;
import PennyPincher.dto.user.UserMapper;
import PennyPincher.entity.User;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
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
        model.addAttribute("loggedInUserName", loggedInUser.getUsername());
        return "profile";
    }

    private boolean doesUserAlreadyExist(String userName) {
        User foundUser = userService.findByUsername(userName);
        return foundUser != null && !foundUser.getUsername().isBlank();
    }

}
