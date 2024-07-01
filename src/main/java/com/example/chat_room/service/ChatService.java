package com.example.chat_room.service;

import com.example.chat_room.model.Chat;
import com.example.chat_room.model.User;
import com.example.chat_room.repository.ChatRepository;
import com.example.chat_room.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<Chat> getChatById(Long id) {
        return chatRepository.findById(id);
    }

    public void save(Chat chat) {
        chatRepository.save(chat);
    }

    public void createGroupChat(String groupName, List<User> users) {
        Chat groupChat = new Chat();
        groupChat.setName(groupName);
        groupChat.setUsers(users);

        chatRepository.save(groupChat); // Save the group chat first

        for (User user : users) {
            user.getChats().add(groupChat);
            userRepository.save(user); // Save user to persist the association
        }
    }

    public boolean chatNameExists(String name) {
        return chatRepository.findByName(name).isPresent();
    }

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
