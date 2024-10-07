## Authors
<p>
  <span><strong>Amit Lap & Yael Karat</strong></span>
  (<a href="https://github.com/Yael-Karat">@Yael-Karat</a>)
</p>

## Chat Room Application

We choose to develop a Chat Room application - Our application combines robust backend functionality with a friendly and Responsive UI to provide a seamless and interactive chat experience. The use of Spring Boot, Spring Security, Thymeleaf, and Bootstrap Ensures the application is secure, maintainable and enhances the UX.

## General Functionality

Our Chat Room application is a web-based chat platform built using the Spring Boot MVC framework. The application leverages Thymeleaf for server-side rendering of HTML views, and Spring Security for user authentication and session management. It provides a platform for users to create accounts, log in, and engage in both private and group chats. Below is a detailed description of the general functionalities provided by the application:

### 1. User Registration and Login
- *Registration*: New users can create an account by providing a unique username and a password. The registration page ensures that the username is not already taken and suggests alternative usernames if needed.
- *Login*: Registered users can log in using their username and password. Upon successful login, the user is redirected to the chat room page. Authentication is handled by Spring Security.

### 2. Chat Room Overview
- *Private Chats*: Users can view the list of their private chats. Each user has a private chat with each one of the other registered users the chat is displayed as the username of the other participant, his online status and the number of unread messages in the chat.
- *Group Chats*: Group chat consists of at least 3 users. users can view the list of their group chats. Each group chat is displays as the group name (selected in the creation of the group chat) and the number of unread messages.
- *Unread Messages*: The application shows the number of unread messages for each chat, helping users quickly identify which chats have new messages.

### 3. User Status Management
- *Online Status*: The application tracks the online status of users. A user is marked as online upon successful login and as offline upon logout.
- *User Online Indicator*: In private chats, the application displays an "Online" badge next to the usernames of users who are currently online.

### 4. Chat Functionality
- *Other user's online status*: The application tracks and displays whether the other user of the chat has logged-in and is online.
- *Message Sending*: Users can send messages in both private and group chats. Messages are displayed with the time they were sent.
- *Message Editing*: Users can edit their messages within a 5-minute window after sending. Edited messages are marked accordingly.
- *Message Deletion*: Users can delete their own messages.
- *Message Liking*: Users can like messages sent by others. Number of likes on the message are displayed.
- *Draft messages*: The application tracks the input when the user is typing a message and stores it and retrieves it whenever the user returns to the chat.

### 5. Group Chat Management
- *Group Chat Creation*: Users can create new group chats by specifying a unique group name and selecting at least 2 users / participants from a list of available users (registered users).
- *Group Chat Suggestions*: If the chosen group chat name already exists, the application suggests alternative names.
- *Draft group chat name*: The application tracks the input when the user is typing the group chat name and stores it and retrieves it whenever the user returns to create a group chat.

### 6. Session Management
- *Session Storage*: The application uses session storage to manage user data and session information securely - tracks the input when the user is typing the group chat name and stores it and retrieves it whenever the user returns to create a group chat.

### 7. Security and Access Control
- *Authentication*: Spring Security is used to authenticate users - each registered user has a unique username and successful log in is when username and password (stored in the DB) match.
- *Authorization*: Access to chat functionality is restricted to authenticated users.

### 8. Additional Features
- *Emoji Picker*: Users can add emojis to their messages using an emoji picker integrated into the chat input area.
- *Scrolling*: The chat box show the bottom of the scrolling window, ensuring that the latest messages are always visible.
- *Page Refresh*: Chat pages automatically refresh at regular intervals to provide a seamless and interactive UX.

## How do you run the application
In application.properties we defined as follows: spring.datasource.url=jdbc:mysql://localhost:3306/ex5?serverTimezone=UTC
<span><p>We will start the MySQL server first.</span></p>
The database needs to be configured with this: utf8mb4_general_ci to be able to store emojis.
Then we will Run the application in intellij.
For a seamless and interactive chat experience a browser should be opened for each user at the same time (at least 3 is recommended for a group chat experience later on).
We will enter http://localhost:8080 url in the browser, and now the site is up and can be used.
In each browser we will register a new user by using a unique username and password of our choice and then connect.
When we open a new tab in the same browser and enter the website, the application will enter chat room page of the same logged-in user.
The chat room screen that shows the chat lists has an indicator that shows whether the other user has logged-in and is online (in private chats).
Messages can be sent both to users who are currently logged-in and to those who are not.
You can create a group chat by choosing a unique group name and selecting at least 2 users from the list of all registered users.
In the chat, you can send messages that include free text and emojis from the application's range of emojis, you can edit the messages you sent (within the delay time - until the option is not displayed), you can delete the message you sent, and you can like or un-like messages sent by other users.
