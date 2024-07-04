package com.example.chat_room.service;

import com.example.chat_room.model.Message;
import com.example.chat_room.model.ReadMessage;
import com.example.chat_room.model.User;
import com.example.chat_room.repository.ReadMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for handling read message-related operations.
 */
@Service
public class ReadMessageService {

    @Autowired
    private ReadMessageRepository readMessageRepository;

    /**
     * Marks a message as read by a user.
     *
     * @param user    the user reading the message
     * @param message the message being read
     */
    public void markMessageAsRead(User user, Message message) {
        ReadMessage readMessage = new ReadMessage();
        readMessage.setUser(user);
        readMessage.setMessage(message);
        readMessageRepository.save(readMessage);
    }

    /**
     * Retrieves the read messages for a user.
     *
     * @param userId the ID of the user
     * @return a list of read messages
     */
    public List<ReadMessage> getReadMessagesByUser(Long userId) {
        return readMessageRepository.findByUserId(userId);
    }
}