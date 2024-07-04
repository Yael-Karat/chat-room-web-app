package com.example.chat_room.service;

import com.example.chat_room.model.User;
import com.example.chat_room.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserStatusService {

    @Autowired
    private UserRepository userRepository;

    public void setUserOnline(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setOnline(true);
            userRepository.save(user);
        }
    }

    public void setUserOffline(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setOnline(false);
            userRepository.save(user);
        }
    }

    public boolean isUserOnline(String username) {
        User user = userRepository.findByUsername(username);
        return user != null && user.isOnline();
    }
}
