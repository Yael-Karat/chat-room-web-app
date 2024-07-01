package com.example.chat_room.repository;

import com.example.chat_room.model.Message;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRepository extends CrudRepository<Message, Long> {
    Optional<Message> findTopByChatIdAndSenderIdOrderByTimestampDesc(Long chatId, Long senderId);
}
