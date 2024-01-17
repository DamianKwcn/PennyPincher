package PennyPincher.controller;

import PennyPincher.service.users.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class LoginController {

    private final UserService userService;



    @GetMapping("/")
    public String redirectToLoginPage() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

}
