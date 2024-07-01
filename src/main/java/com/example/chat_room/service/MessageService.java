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

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ReadMessageRepository readMessageRepository;

    public Optional<Message> getMessageById(Long id) {
        return messageRepository.findById(id);
    }

    public void save(Message message) {
        messageRepository.save(message);
    }

    @Transactional
    public void deleteMessage(Message message) {
        // Find related ReadMessage entities and delete them
        List<ReadMessage> readMessages = readMessageRepository.findByMessage(message);
        readMessageRepository.deleteAll(readMessages);

        // Delete the Message entity
        messageRepository.delete(message);
    }

    public void likeMessage(Message message, User user) {
        message.getLikes().add(user);
        messageRepository.save(message);
    }

    public void unlikeMessage(Message message, User user) {
        message.getLikes().remove(user);
        messageRepository.save(message);
    }

    public Optional<Message> getLastMessageSentByUserInChat(Long chatId, Long userId) {
        return messageRepository.findTopByChatIdAndSenderIdOrderByTimestampDesc(chatId, userId);
    }
}
