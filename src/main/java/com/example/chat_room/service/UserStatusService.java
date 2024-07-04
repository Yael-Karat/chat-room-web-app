package com.example.chat_room.service;

import com.example.chat_room.model.User;
import com.example.chat_room.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for handling user status-related operations.
 */
@Service
public class UserStatusService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Sets a user as online.
     *
     * @param username the username of the user
     */
    public void setUserOnline(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setOnline(true);
            userRepository.save(user);
        }
    }

    /**
     * Sets a user as offline.
     *
     * @param username the username of the user
     */
    public void setUserOffline(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setOnline(false);
            userRepository.save(user);
        }
    }

    /**
     * Checks if a user is online.
     *
     * @param username the username of the user
     * @return true if the user is online, false otherwise
     */
    public boolean isUserOnline(String username) {
        User user = userRepository.findByUsername(username);
        return user != null && user.isOnline();
    }
}