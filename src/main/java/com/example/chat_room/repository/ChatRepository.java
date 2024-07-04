package com.example.chat_room.repository;

import com.example.chat_room.model.Chat;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Chat entity.
 */
@Repository
public interface ChatRepository extends CrudRepository<Chat, Long> {
    Optional<Chat> findByName(String name);
}