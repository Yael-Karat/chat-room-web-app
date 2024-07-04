package com.example.chat_room.repository;

import com.example.chat_room.model.Message;
import com.example.chat_room.model.ReadMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ReadMessage entity.
 */
@Repository
public interface ReadMessageRepository extends CrudRepository<ReadMessage, Long> {
    List<ReadMessage> findByUserId(Long userId);
    List<ReadMessage> findByMessage(Message message);
}