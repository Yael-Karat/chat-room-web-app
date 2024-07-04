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

/**
 * Service for handling user-related operations.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Saves a user entity and creates private chats with existing users.
     *
     * @param user the user entity to save
     */
    public void saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        Iterable<User> users = userRepository.findAll();
        for (User existingUser : users) {
            if (!existingUser.getUsername().equals(user.getUsername())) {
                Chat chat = new Chat();
                chat.getUsers().add(existingUser);
                chat.getUsers().add(user);
                chatRepository.save(chat);

                existingUser.getChats().add(chat);
                user.getChats().add(chat);

                userRepository.save(existingUser);
            }
        }
        userRepository.save(user);
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username the username of the user
     * @return the user entity if found
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Checks if a username already exists.
     *
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username) != null;
    }

    /**
     * Suggests an alternative username.
     *
     * @param username the base username
     * @return a suggested alternative username
     */
    public String suggestUsername(String username) {
        String suggestedUsername;
        int counter = 1;
        do {
            suggestedUsername = username + counter;
            counter++;
        } while (usernameExists(suggestedUsername));
        return suggestedUsername;
    }

    /**
     * Retrieves all users.
     *
     * @return a list of all users
     */
    public List<User> findAllUsers() {
        return (List<User>) userRepository.findAll();
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id the ID of the user
     * @return the user entity if found
     */
    public User findById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }
}