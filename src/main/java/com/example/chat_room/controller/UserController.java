package com.example.chat_room.controller;

import com.example.chat_room.model.User;
import com.example.chat_room.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        if (userService.usernameExists(user.getUsername())) {
            String suggestedUsername = userService.suggestUsername(user.getUsername());
            model.addAttribute("errorMessage", "Username already exists. Suggested username: " + suggestedUsername);
            model.addAttribute("suggestedUsername", suggestedUsername);
            return "register";
        }

        userService.saveUser(user);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("success", true);
        return "register";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
}
