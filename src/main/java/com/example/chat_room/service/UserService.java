package com.example.chat_room.service;

import com.example.chat_room.model.Chat;
import com.example.chat_room.model.User;
import com.example.chat_room.repository.ChatRepository;
import com.example.chat_room.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public void saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        // Create chat with existing users
        Iterable<User> users = userRepository.findAll();
        for (User existingUser : users) {
            if (!existingUser.getUsername().equals(user.getUsername())) {
                Chat chat = new Chat();
                chat.getUsers().add(existingUser);
                chat.getUsers().add(user);
                chatRepository.save(chat);

                // Add the chat to the users
                existingUser.getChats().add(chat);
                user.getChats().add(chat);

                userRepository.save(existingUser);
            }
        }
        userRepository.save(user); // Save the user again to persist the chat relationships
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username) != null;
    }

    public String suggestUsername(String username) {
        String suggestedUsername;
        int counter = 1;
        do {
            suggestedUsername = username + counter;
            counter++;
        } while (usernameExists(suggestedUsername));
        return suggestedUsername;
    }

    public List<User> findAllUsers() {
        return (List<User>) userRepository.findAll();
    }

    public User findById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }
}
