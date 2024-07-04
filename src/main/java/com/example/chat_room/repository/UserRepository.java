package com.example.chat_room.repository;

import com.example.chat_room.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for User entity.
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    User findByUsername(String username);
}