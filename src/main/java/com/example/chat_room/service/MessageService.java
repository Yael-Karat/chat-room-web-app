package com.example.chat_room.service;

import com.example.chat_room.model.Message;
import com.example.chat_room.model.ReadMessage;
import com.example.chat_room.model.User;
import com.example.chat_room.repository.MessageRepository;
import com.example.chat_room.repository.ReadMessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for handling message-related operations.
 */
@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ReadMessageRepository readMessageRepository;

    /**
     * Retrieves a message by its ID.
     *
     * @param id the ID of the message
     * @return an Optional containing the message if found
     */
    public Optional<Message> getMessageById(Long id) {
        return messageRepository.findById(id);
    }

    /**
     * Saves a message entity.
     *
     * @param message the message entity to save
     */
    public void save(Message message) {
        messageRepository.save(message);
    }

    /**
     * Deletes a message and its associated read messages.
     *
     * @param message the message entity to delete
     */
    @Transactional
    public void deleteMessage(Message message) {
        List<ReadMessage> readMessages = readMessageRepository.findByMessage(message);
        readMessageRepository.deleteAll(readMessages);
        messageRepository.delete(message);
    }

    /**
     * Adds a like to a message from a user.
     *
     * @param message the message to like
     * @param user    the user liking the message
     */
    public void likeMessage(Message message, User user) {
        message.getLikes().add(user);
        messageRepository.save(message);
    }

    /**
     * Removes a like from a message by a user.
     *
     * @param message the message to unlike
     * @param user    the user unliking the message
     */
    public void unlikeMessage(Message message, User user) {
        message.getLikes().remove(user);
        messageRepository.save(message);
    }

    /**
     * Retrieves the most recent message sent by a user in a chat.
     *
     * @param chatId the ID of the chat
     * @param userId the ID of the user
     * @return an Optional containing the message if found
     */
    public Optional<Message> getLastMessageSentByUserInChat(Long chatId, Long userId) {
        return messageRepository.findTopByChatIdAndSenderIdOrderByTimestampDesc(chatId, userId);
    }
}