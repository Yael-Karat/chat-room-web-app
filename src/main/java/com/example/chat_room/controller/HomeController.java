package com.example.chat_room.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling home page requests.
 */
@Controller
public class HomeController {

    /**
     * Displays the home page.
     *
     * @return the index view
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }
}