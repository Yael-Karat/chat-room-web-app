package com.example.chat_room.service;

import com.example.chat_room.model.Chat;
import com.example.chat_room.model.User;
import com.example.chat_room.repository.ChatRepository;
import com.example.chat_room.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for handling chat-related operations.
 */
@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves a chat by its ID.
     *
     * @param id the ID of the chat
     * @return an Optional containing the chat if found
     */
    public Optional<Chat> getChatById(Long id) {
        return chatRepository.findById(id);
    }

    /**
     * Saves a chat entity.
     *
     * @param chat the chat entity to save
     */
    public void save(Chat chat) {
        chatRepository.save(chat);
    }

    /**
     * Creates a new group chat with the specified users.
     *
     * @param groupName the name of the group chat
     * @param users     the list of users in the group chat
     */
    public void createGroupChat(String groupName, List<User> users) {
        Chat groupChat = new Chat();
        groupChat.setName(groupName);
        groupChat.setUsers(users);

        chatRepository.save(groupChat);

        for (User user : users) {
            user.getChats().add(groupChat);
            userRepository.save(user);
        }
    }

    /**
     * Checks if a chat name already exists.
     *
     * @param name the name of the chat
     * @return true if the chat name exists, false otherwise
     */
    public boolean chatNameExists(String name) {
        return chatRepository.findByName(name).isPresent();
    }

    /**
     * Suggests an alternative name for a chat.
     *
     * @param baseName the base name of the chat
     * @return a suggested alternative name
     */
    public String suggestAlternativeName(String baseName) {
        String suggestedName = baseName;
        int counter = 1;
        while (chatNameExists(suggestedName)) {
            suggestedName = baseName + " " + counter;
            counter++;
        }
        return suggestedName;
    }
}