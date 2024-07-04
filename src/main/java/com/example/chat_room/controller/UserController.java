package com.example.chat_room.controller;

import com.example.chat_room.model.User;
import com.example.chat_room.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Controller for handling user-related requests.
 */
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Displays the registration form.
     *
     * @param model the model to hold attributes for the view
     * @return the register view
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    /**
     * Handles user registration.
     *
     * @param user  the user to register
     * @param model the model to hold attributes for the view
     * @return the register view with a success or error message
     */
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

    /**
     * Displays the login form.
     *
     * @return the login view
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
}