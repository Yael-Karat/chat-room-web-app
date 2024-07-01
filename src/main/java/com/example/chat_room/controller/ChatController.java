package com.example.chat_room.controller;

import com.example.chat_room.model.Chat;
import com.example.chat_room.model.Message;
import com.example.chat_room.model.User;
import com.example.chat_room.model.ReadMessage;
import com.example.chat_room.service.ChatService;
import com.example.chat_room.service.MessageService;
import com.example.chat_room.service.ReadMessageService;
import com.example.chat_room.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ReadMessageService readMessageService;

    @GetMapping("/chatroom")
    public String showChatroom(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        User user = userService.findByUsername(currentUser.getUsername());
        if (user == null) {
            throw new RuntimeException("Authenticated user not found in the database");
        }

        // Retrieve all chats where the user is a participant
        List<Chat> userChats = user.getChats();

        // Separate private and group chats
        List<Chat> privateChats = userChats.stream()
                .filter(chat -> chat.getUsers().size() == 2)
                .collect(Collectors.toList());

        List<Chat> groupChats = userChats.stream()
                .filter(chat -> chat.getUsers().size() > 2)
                .collect(Collectors.toList());

        // Calculate unread messages
        Map<Long, Long> unreadMessagesCount = new HashMap<>();
        for (Chat chat : userChats) {
            long unreadCount = chat.getMessages().stream()
                    .filter(message -> message.getTimestamp().isAfter(
                            readMessageService.getReadMessagesByUser(user.getId()).stream()
                                    .map(ReadMessage::getMessage)
                                    .filter(readMessageMessage -> readMessageMessage.equals(message))
                                    .map(Message::getTimestamp)
                                    .max(LocalDateTime::compareTo)
                                    .orElse(LocalDateTime.MIN)
                    ))
                    .count();
            unreadMessagesCount.put(chat.getId(), unreadCount);
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("privateChats", privateChats);
        model.addAttribute("groupChats", groupChats);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);
        return "chatroom";
    }

    @GetMapping("/chat/{id}")
    public String showChat(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<Chat> chatOptional = chatService.getChatById(id);
        if (chatOptional.isPresent()) {
            Chat chat = chatOptional.get();
            User currentUser = userService.findByUsername(userDetails.getUsername());

            // Ensure the user is part of the chat
            if (!chat.getUsers().contains(currentUser)) {
                throw new RuntimeException("User is not part of this chat");
            }

            // Format timestamps and add to model
            List<Message> messages = chat.getMessages();
            List<Long> editableMessageIds = new ArrayList<>();
            for (Message message : messages) {
                message.setFormattedTimestamp(message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")));
                readMessageService.markMessageAsRead(currentUser, message); // Mark message as read
                if (message.getSender().equals(currentUser) && Duration.between(message.getTimestamp(), LocalDateTime.now()).toMinutes() < 2) {
                    editableMessageIds.add(message.getId());
                }
            }

            model.addAttribute("chat", chat);
            model.addAttribute("messages", chat.getMessages());
            model.addAttribute("currentUser", currentUser); // Add this line to include currentUser in the model
            model.addAttribute("editableMessageIds", editableMessageIds); // Add list of editable message IDs
            return "chat";
        } else {
            throw new RuntimeException("Chat not found");
        }
    }

    @PostMapping("/chat/{id}/send")
    public String sendMessage(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam String messageContent, Model model) {
        Optional<Chat> chatOptional = chatService.getChatById(id);

        if (chatOptional.isPresent()) {
            Chat chat = chatOptional.get();
            User currentUser = userService.findByUsername(userDetails.getUsername());

            // Ensure the user is part of the chat
            if (!chat.getUsers().contains(currentUser)) {
                throw new RuntimeException("User is not part of this chat");
            }

            // Create and save the new message
            Message message = new Message();
            message.setContent(messageContent);
            message.setSender(currentUser);
            message.setChat(chat);
            LocalDateTime timestamp = LocalDateTime.now();
            message.setTimestamp(timestamp);
            chat.getMessages().add(message);
            chatService.save(chat);

            return "redirect:/chat/" + id;
        } else {
            throw new RuntimeException("Chat not found");
        }
    }

    @PostMapping("/chat/{chatId}/message/{messageId}/like")
    public String likeMessage(@PathVariable Long chatId, @PathVariable Long messageId, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        Optional<Message> messageOptional = messageService.getMessageById(messageId);
        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();
            if (!message.getLikes().contains(currentUser)) {
                messageService.likeMessage(message, currentUser);
            }
        }
        return "redirect:/chat/" + chatId;
    }

    @PostMapping("/chat/{chatId}/message/{messageId}/unlike")
    public String unlikeMessage(@PathVariable Long chatId, @PathVariable Long messageId, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        Optional<Message> messageOptional = messageService.getMessageById(messageId);
        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();
            if (message.getLikes().contains(currentUser)) {
                messageService.unlikeMessage(message, currentUser);
            }
        }
        return "redirect:/chat/" + chatId;
    }

    @PostMapping("/chat/{chatId}/message/{messageId}/delete")
    public String deleteMessage(@PathVariable Long chatId, @PathVariable Long messageId, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Chat> chatOptional = chatService.getChatById(chatId);
        if (chatOptional.isPresent()) {
            Chat chat = chatOptional.get();
            User currentUser = userService.findByUsername(userDetails.getUsername());

            // Ensure the user is part of the chat
            if (!chat.getUsers().contains(currentUser)) {
                throw new RuntimeException("User is not part of this chat");
            }

            Optional<Message> messageOptional = messageService.getMessageById(messageId);
            if (messageOptional.isPresent()) {
                Message message = messageOptional.get();
                // Ensure the user is the sender of the message
                if (message.getSender().equals(currentUser)) {
                    chat.getMessages().remove(message);
                    messageService.deleteMessage(message);
                } else {
                    throw new RuntimeException("User is not the sender of this message");
                }
            } else {
                throw new RuntimeException("Message not found");
            }

            return "redirect:/chat/" + chatId;
        } else {
            throw new RuntimeException("Chat not found");
        }
    }

    @GetMapping("/chat/{chatId}/message/{messageId}/edit")
    public String showEditMessageForm(@PathVariable Long chatId, @PathVariable Long messageId, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<Message> messageOptional = messageService.getMessageById(messageId);
        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();
            User currentUser = userService.findByUsername(userDetails.getUsername());

            // Ensure the user is the sender of the message
            if (!message.getSender().equals(currentUser)) {
                throw new RuntimeException("User is not the sender of this message");
            }

            // Ensure the message is within the editable time window
            if (Duration.between(message.getTimestamp(), LocalDateTime.now()).toMinutes() >= 2) {
                throw new RuntimeException("Cannot edit the message after 2 minutes");
            }

            model.addAttribute("message", message);
            return "editMessage";
        } else {
            throw new RuntimeException("Message not found");
        }
    }

    @PostMapping("/chat/{chatId}/message/{messageId}/edit")
    public String editMessage(@PathVariable Long chatId, @PathVariable Long messageId, @RequestParam String messageContent, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Message> messageOptional = messageService.getMessageById(messageId);
        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();
            User currentUser = userService.findByUsername(userDetails.getUsername());

            // Ensure the user is the sender of the message
            if (!message.getSender().equals(currentUser)) {
                throw new RuntimeException("User is not the sender of this message");
            }

            // Ensure the message is within the editable time window
            if (Duration.between(message.getTimestamp(), LocalDateTime.now()).toMinutes() >= 2) {
                throw new RuntimeException("Cannot edit the message after 2 minutes");
            }

            message.setContent(messageContent);
            messageService.save(message);

            return "redirect:/chat/" + chatId;
        } else {
            throw new RuntimeException("Message not found");
        }
    }

    @GetMapping("/createGroupChat")
    public String showCreateGroupChatForm(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        User user = userService.findByUsername(currentUser.getUsername());
        model.addAttribute("users", userService.findAllUsers().stream()
                .filter(u -> !u.getId().equals(user.getId()))
                .collect(Collectors.toList()));
        model.addAttribute("currentUser", user); // Add currentUser to the model
        return "createGroupChat";
    }

    @PostMapping("/createGroupChat")
    public String createGroupChat(@RequestParam String groupName, @RequestParam List<Long> userIds, @AuthenticationPrincipal UserDetails currentUser, Model model) {
        User user = userService.findByUsername(currentUser.getUsername());
        List<User> users = new ArrayList<>();
        users.add(user);
        for (Long userId : userIds) {
            User u = userService.findById(userId);
            if (u != null) {
                users.add(u);
            }
        }

        if (users.size() < 3) {
            model.addAttribute("errorMessage", "A group chat must have at least 3 users.");
            model.addAttribute("users", userService.findAllUsers().stream()
                    .filter(u -> !u.getId().equals(user.getId()))
                    .collect(Collectors.toList()));
            model.addAttribute("currentUser", user); // Add currentUser to the model
            return "createGroupChat";
        }

        if (chatService.chatNameExists(groupName)) {
            String suggestedName = chatService.suggestAlternativeName(groupName);
            model.addAttribute("errorMessage", "Group chat name already exists. Suggested name: " + suggestedName);
            model.addAttribute("users", userService.findAllUsers().stream()
                    .filter(u -> !u.getId().equals(user.getId()))
                    .collect(Collectors.toList()));
            model.addAttribute("currentUser", user); // Add currentUser to the model
            return "createGroupChat";
        }

        chatService.createGroupChat(groupName, users);
        return "redirect:/chatroom";
    }

    @GetMapping("/groupChat/{id}")
    public String showGroupChat(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<Chat> chatOptional = chatService.getChatById(id);
        if (chatOptional.isPresent()) {
            Chat chat = chatOptional.get();
            User currentUser = userService.findByUsername(userDetails.getUsername());

            // Ensure the user is part of the chat
            if (!chat.getUsers().contains(currentUser)) {
                throw new RuntimeException("User is not part of this chat");
            }

            // Format timestamps and add to model
            List<Message> messages = chat.getMessages();
            List<Long> editableMessageIds = new ArrayList<>();
            for (Message message : messages) {
                message.setFormattedTimestamp(message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")));
                readMessageService.markMessageAsRead(currentUser, message); // Mark message as read
                if (message.getSender().equals(currentUser) && Duration.between(message.getTimestamp(), LocalDateTime.now()).toMinutes() < 2) {
                    editableMessageIds.add(message.getId());
                }
            }

            model.addAttribute("chat", chat);
            model.addAttribute("messages", chat.getMessages());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("editableMessageIds", editableMessageIds); // Add list of editable message IDs
            return "chat"; // This should match the name of your chat view template
        } else {
            throw new RuntimeException("Chat not found");
        }
    }
}
