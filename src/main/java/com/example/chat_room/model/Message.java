package com.example.chat_room.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a message.
 */
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User sender;

    @ManyToMany
    @JoinTable(
            name = "message_likes",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> likes = new ArrayList<>();

    private String content;
    private LocalDateTime timestamp;

    @Transient
    private String formattedTimestamp;

    private boolean edited;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<User> getLikes() {
        return likes;
    }

    public void setLikes(List<User> likes) {
        this.likes = likes;
    }

    public int getLikeCount() {
        return likes.size();
    }

    public String getFormattedTimestamp() {
        return formattedTimestamp;
    }

    public void setFormattedTimestamp(String formattedTimestamp) {
        this.formattedTimestamp = formattedTimestamp;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    // Override toString for better logging and debugging
    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", chat=" + chat +
                ", sender=" + sender +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", edited=" + edited +
                '}';
    }
}