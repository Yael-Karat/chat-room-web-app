/* js from chatroom.html */
function clearStorage() {
    localStorage.clear();
    sessionStorage.clear();
}
/* js from chat.html */
// Scrolling to bottom when page loads completely
function scrollToBottom() {
    const chatBox = document.getElementById('chatBox');
    chatBox.scrollTop = chatBox.scrollHeight;
}

// Handling the adding the Emojis to the message and saving draft message
function insertEmoji(emoji) {
    let input = document.getElementById("messageContent");
    input.value += emoji;
    saveDraftMessage();
}

$(document).ready(function(){
    $('[data-bs-toggle="tooltip"]').tooltip();
});

/* js from editMessage.html */
// Handling the adding the Emojis to the message
function insertEmojiEdit(emoji) {
    let input = document.getElementById("messageContent");
    input.value += emoji;
}

/* js from createGroupChat.html */
// Saving the group chat name input using session
function saveDraftGroupName() {
    const groupName = document.getElementById('groupName').value;
    sessionStorage.setItem('draftGroupName', groupName);
}

// Retrieving group chat name input using session
function loadDraftGroupName() {
    const draftGroupName = sessionStorage.getItem('draftGroupName');
    if (draftGroupName) {
        document.getElementById('groupName').value = draftGroupName;
    }
}

// Clearing the group chat name from session storage when done
function clearSessionStorage() {
    sessionStorage.removeItem('draftGroupName');
}