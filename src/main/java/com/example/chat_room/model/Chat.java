package com.example.chat_room.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a chat.
 */
@Entity
@Table(name = "chats")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "chats")
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    // Constructors
    public Chat() {
    }

    /**
     * Helper method to get the other user in a private chat.
     *
     * @param currentUser the current user
     * @return the other user in the chat
     */
    public User getOtherUser(User currentUser) {
        return users.stream().filter(user -> !user.equals(currentUser)).findFirst().orElse(null);
    }

    // Override toString for better logging and debugging
    @Override
    public String toString() {
        return "Chat{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", users=" + users +
                ", messages=" + messages +
                '}';
    }
}