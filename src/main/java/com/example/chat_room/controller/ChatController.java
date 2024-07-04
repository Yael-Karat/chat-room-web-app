package com.example.chat_room.controller;

import com.example.chat_room.model.Chat;
import com.example.chat_room.model.Message;
import com.example.chat_room.model.User;
import com.example.chat_room.model.ReadMessage;
import com.example.chat_room.service.ChatService;
import com.example.chat_room.service.MessageService;
import com.example.chat_room.service.ReadMessageService;
import com.example.chat_room.service.UserService;
import com.example.chat_room.service.UserStatusService;
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

import jakarta.servlet.http.HttpSession;

/**
 * Controller for handling chat-related operations.
 */
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

    @Autowired
    private UserStatusService userStatusService;

    /**
     * Displays the chatroom page with a list of private and group chats.
     *
     * @param currentUser the currently authenticated user
     * @param model       the model to hold attributes for the view
     * @return the chatroom view
     */
    @GetMapping("/chatroom")
    public String showChatroom(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        User user = userService.findByUsername(currentUser.getUsername());
        if (user == null) {
            System.err.println("Authenticated user not found in the database: " + currentUser.getUsername());
            return "redirect:/login?error=userNotFound";
        }

        List<Chat> userChats = user.getChats();

        List<Chat> privateChats = userChats.stream()
                .filter(chat -> chat.getUsers().size() == 2)
                .collect(Collectors.toList());

        List<Chat> groupChats = userChats.stream()
                .filter(chat -> chat.getUsers().size() > 2)
                .collect(Collectors.toList());

        Map<Long, Long> unreadMessagesCount = calculateUnreadMessagesCount(user, userChats);

        model.addAttribute("currentUser", user);
        model.addAttribute("privateChats", privateChats);
        model.addAttribute("groupChats", groupChats);
        model.addAttribute("unreadMessagesCount", unreadMessagesCount);
        model.addAttribute("userStatusService", userStatusService);
        return "chatroom";
    }

    /**
     * Displays the chat page for a specific chat.
     *
     * @param id          the ID of the chat
     * @param userDetails the details of the currently authenticated user
     * @param model       the model to hold attributes for the view
     * @param session     the HTTP session
     * @return the chat view
     */
    @GetMapping("/chat/{id}")
    public String showChat(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model, HttpSession session) {
        Optional<Chat> chatOptional = chatService.getChatById(id);
        if (chatOptional.isPresent()) {
            Chat chat = chatOptional.get();
            User currentUser = userService.findByUsername(userDetails.getUsername());

            if (!chat.getUsers().contains(currentUser)) {
                throw new RuntimeException("User is not part of this chat");
            }

            List<Message> messages = chat.getMessages();
            List<Long> editableMessageIds = new ArrayList<>();
            formatAndMarkMessagesAsRead(currentUser, messages, editableMessageIds);

            String draftMessage = (String) session.getAttribute("draftMessage_" + currentUser.getId() + "_" + chat.getId());

            model.addAttribute("chat", chat);
            model.addAttribute("messages", messages);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("editableMessageIds", editableMessageIds);
            model.addAttribute("draftMessage", draftMessage);
            model.addAttribute("userStatusService", userStatusService);
            return "chat";
        } else {
            throw new RuntimeException("Chat not found");
        }
    }

    /**
     * Handles the sending of a message in a specific chat.
     *
     * @param id             the ID of the chat
     * @param userDetails    the details of the currently authenticated user
     * @param messageContent the content of the message to be sent
     * @param model          the model to hold attributes for the view
     * @param session        the HTTP session
     * @return a redirect to the chat view
     */
    @PostMapping("/chat/{id}/send")
    public String sendMessage(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam String messageContent, Model model, HttpSession session) {
        Optional<Chat> chatOptional = chatService.getChatById(id);

        if (chatOptional.isPresent()) {
            Chat chat = chatOptional.get();
            User currentUser = userService.findByUsername(userDetails.getUsername());

            if (!chat.getUsers().contains(currentUser)) {
                throw new RuntimeException("User is not part of this chat");
            }

            Message message = new Message();
            message.setContent(messageContent);
            message.setSender(currentUser);
            message.setChat(chat);
            LocalDateTime timestamp = LocalDateTime.now();
            message.setTimestamp(timestamp);
            chat.getMessages().add(message);
            chatService.save(chat);

            session.removeAttribute("draftMessage_" + currentUser.getId() + "_" + chat.getId());

            model.addAttribute("chatId", chat.getId());
            model.addAttribute("currentUserId", currentUser.getId());

            return "redirect:/chat/" + id;
        } else {
            throw new RuntimeException("Chat not found");
        }
    }

    /**
     * Handles the liking of a message.
     *
     * @param chatId     the ID of the chat
     * @param messageId  the ID of the message to be liked
     * @param userDetails the details of the currently authenticated user
     * @return a redirect to the chat view
     */
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

    /**
     * Handles the unliking of a message.
     *
     * @param chatId     the ID of the chat
     * @param messageId  the ID of the message to be unliked
     * @param userDetails the details of the currently authenticated user
     * @return a redirect to the chat view
     */
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

    /**
     * Handles the deletion of a message.
     *
     * @param chatId     the ID of the chat
     * @param messageId  the ID of the message to be deleted
     * @param userDetails the details of the currently authenticated user
     * @return a redirect to the chat view
     */
    @PostMapping("/chat/{chatId}/message/{messageId}/delete")
    public String deleteMessage(@PathVariable Long chatId, @PathVariable Long messageId, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Chat> chatOptional = chatService.getChatById(chatId);
        if (chatOptional.isPresent()) {
            Chat chat = chatOptional.get();
            User currentUser = userService.findByUsername(userDetails.getUsername());

            if (!chat.getUsers().contains(currentUser)) {
                throw new RuntimeException("User is not part of this chat");
            }

            Optional<Message> messageOptional = messageService.getMessageById(messageId);
            if (messageOptional.isPresent()) {
                Message message = messageOptional.get();
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

    /**
     * Displays the form for editing a message.
     *
     * @param chatId     the ID of the chat
     * @param messageId  the ID of the message to be edited
     * @param userDetails the details of the currently authenticated user
     * @param model       the model to hold attributes for the view
     * @return the edit message view
     */
    @GetMapping("/chat/{chatId}/message/{messageId}/edit")
    public String showEditMessageForm(@PathVariable Long chatId, @PathVariable Long messageId, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<Message> messageOptional = messageService.getMessageById(messageId);
        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();
            User currentUser = userService.findByUsername(userDetails.getUsername());

            if (!message.getSender().equals(currentUser)) {
                throw new RuntimeException("User is not the sender of this message");
            }

            if (Duration.between(message.getTimestamp(), LocalDateTime.now()).toMinutes() >= 2) {
                throw new RuntimeException("Cannot edit the message after 2 minutes");
            }

            model.addAttribute("message", message);
            return "editMessage";
        } else {
            throw new RuntimeException("Message not found");
        }
    }

    /**
     * Handles the editing of a message.
     *
     * @param chatId      the ID of the chat
     * @param messageId   the ID of the message to be edited
     * @param messageContent the new content of the message
     * @param userDetails the details of the currently authenticated user
     * @return a redirect to the chat view
     */
    @PostMapping("/chat/{chatId}/message/{messageId}/edit")
    public String editMessage(@PathVariable Long chatId, @PathVariable Long messageId, @RequestParam String messageContent, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Message> messageOptional = messageService.getMessageById(messageId);
        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();
            User currentUser = userService.findByUsername(userDetails.getUsername());

            if (!message.getSender().equals(currentUser)) {
                throw new RuntimeException("User is not the sender of this message");
            }

            if (Duration.between(message.getTimestamp(), LocalDateTime.now()).toMinutes() >= 2) {
                throw new RuntimeException("Cannot edit the message after 2 minutes");
            }

            message.setContent(messageContent);
            message.setEdited(true);  // Marking the message as edited
            messageService.save(message);

            return "redirect:/chat/" + chatId;
        } else {
            throw new RuntimeException("Message not found");
        }
    }

    /**
     * Displays the form for creating a new group chat.
     *
     * @param model       the model to hold attributes for the view
     * @param currentUser the currently authenticated user
     * @return the create group chat view
     */
    @GetMapping("/createGroupChat")
    public String showCreateGroupChatForm(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        User user = userService.findByUsername(currentUser.getUsername());
        model.addAttribute("users", userService.findAllUsers().stream()
                .filter(u -> !u.getId().equals(user.getId()))
                .collect(Collectors.toList()));
        model.addAttribute("currentUser", user); // Adding currentUser to the model
        return "createGroupChat";
    }

    /**
     * Handles the creation of a new group chat.
     *
     * @param groupName   the name of the group chat
     * @param userIds     the IDs of the users to be added to the group chat
     * @param currentUser the currently authenticated user
     * @param model       the model to hold attributes for the view
     * @return the create group chat view
     */
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

        if (chatService.chatNameExists(groupName)) {
            String suggestedName = chatService.suggestAlternativeName(groupName);
            model.addAttribute("errorMessage", "Group chat name already exists. Suggested name: " + suggestedName);
            model.addAttribute("users", userService.findAllUsers().stream()
                    .filter(u -> !u.getId().equals(user.getId()))
                    .collect(Collectors.toList()));
            model.addAttribute("currentUser", user); // Adding currentUser to the model
            return "createGroupChat";
        }

        chatService.createGroupChat(groupName, users);
        model.addAttribute("successMessage", "Group chat created successfully.");
        model.addAttribute("users", userService.findAllUsers().stream()
                .filter(u -> !u.getId().equals(user.getId()))
                .collect(Collectors.toList()));
        model.addAttribute("currentUser", user); // Adding currentUser to the model
        return "createGroupChat";
    }

    /**
     * Displays the group chat page for a specific group chat.
     *
     * @param id          the ID of the group chat
     * @param userDetails the details of the currently authenticated user
     * @param model       the model to hold attributes for the view
     * @param session     the HTTP session
     * @return the chat view
     */
    @GetMapping("/groupChat/{id}")
    public String showGroupChat(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model, HttpSession session) {
        Optional<Chat> chatOptional = chatService.getChatById(id);
        if (chatOptional.isPresent()) {
            Chat chat = chatOptional.get();
            User currentUser = userService.findByUsername(userDetails.getUsername());

            if (!chat.getUsers().contains(currentUser)) {
                throw new RuntimeException("User is not part of this chat");
            }

            List<Message> messages = chat.getMessages();
            List<Long> editableMessageIds = new ArrayList<>();
            formatAndMarkMessagesAsRead(currentUser, messages, editableMessageIds);

            String draftMessage = (String) session.getAttribute("draftMessage_" + currentUser.getId() + "_" + chat.getId());

            model.addAttribute("chat", chat);
            model.addAttribute("messages", messages);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("editableMessageIds", editableMessageIds);
            model.addAttribute("draftMessage", draftMessage);
            return "chat";
        } else {
            throw new RuntimeException("Chat not found");
        }
    }

    /**
     * Calculates the unread messages count for each chat.
     *
     * @param user      the currently authenticated user
     * @param userChats the list of chats the user is part of
     * @return a map of chat IDs to unread messages count
     */
    private Map<Long, Long> calculateUnreadMessagesCount(User user, List<Chat> userChats) {
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
        return unreadMessagesCount;
    }

    /**
     * Formats messages and marks them as read for the current user.
     *
     * @param currentUser        the currently authenticated user
     * @param messages           the list of messages to format and mark as read
     * @param editableMessageIds the list of message IDs that are editable
     */
    private void formatAndMarkMessagesAsRead(User currentUser, List<Message> messages, List<Long> editableMessageIds) {
        messages.sort(Comparator.comparing(Message::getTimestamp));
        for (Message message : messages) {
            message.setFormattedTimestamp(message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")));
            readMessageService.markMessageAsRead(currentUser, message);
            if (message.getSender().equals(currentUser) && Duration.between(message.getTimestamp(), LocalDateTime.now()).toMinutes() < 2) {
                editableMessageIds.add(message.getId());
            }
        }
    }
}