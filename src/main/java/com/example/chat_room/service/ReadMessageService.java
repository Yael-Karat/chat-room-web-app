package com.example.chat_room.service;

import com.example.chat_room.model.Message;
import com.example.chat_room.model.ReadMessage;
import com.example.chat_room.model.User;
import com.example.chat_room.repository.ReadMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReadMessageService {

    @Autowired
    private ReadMessageRepository readMessageRepository;

    public void markMessageAsRead(User user, Message message) {
        ReadMessage readMessage = new ReadMessage();
        readMessage.setUser(user);
        readMessage.setMessage(message);
        readMessageRepository.save(readMessage);
    }

    public List<ReadMessage> getReadMessagesByUser(Long userId) {
        return readMessageRepository.findByUserId(userId);
    }
}
