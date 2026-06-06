<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Live Chat Admin</title>
    <style>
        body { font-family: Arial, sans-serif; display: flex; height: 100vh; margin: 0; }
        .sidebar { width: 250px; background: #f4f4f4; border-right: 1px solid #ddd; overflow-y: auto; }
        .chat-container { flex: 1; display: flex; flex-direction: column; }
        .chat-header { padding: 15px; background: #007bff; color: white; }
        .messages { flex: 1; overflow-y: auto; padding: 15px; background: #fafafa; }
        .message-box { display: flex; padding: 15px; border-top: 1px solid #ddd; background: white; }
        .message-box input { flex: 1; padding: 10px; border: 1px solid #ccc; border-radius: 4px; margin-right: 10px; }
        .message-box button { padding: 10px 20px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; }
        .message { margin-bottom: 10px; padding: 10px; border-radius: 5px; max-width: 70%; line-height: 1.4; }
        .user-message { background: #e9ecef; align-self: flex-start; }
        .admin-message { background: #007bff; color: white; margin-left: auto; align-self: flex-end; }
        .chat-list-item { padding: 15px; border-bottom: 1px solid #ddd; cursor: pointer; }
        .chat-list-item:hover { background: #e9ecef; }
    </style>
</head>
<body>

<div class="sidebar" id="sidebar">
    <h3 style="padding: 15px; margin: 0; background: #eee;">Active Chats</h3>
    <div id="chat-list"></div>
</div>

<div class="chat-container">
    <div class="chat-header">
        <h2 id="chat-title" style="margin:0; font-size: 1.2rem;">Select a chat</h2>
    </div>
    <div class="messages" id="messages"></div>
    <div class="message-box">
        <input type="text" id="message-input" placeholder="Type a message..." disabled>
        <!-- Distinct non-overlapping buttons for Admin Chat UI -->
        <button id="chatbot-toggle-btn" style="background: #28a745; margin-right: 10px;" disabled onclick="autoReply()">Auto-Reply ✨</button>
        <button id="send-btn" disabled onclick="sendMessage()">Send Message</button>
    </div>
</div>

<script>
    let activeUserId = null;
    let autoRefreshTimer = null;

    // Load active users list
    async function loadActiveChats() {
        try {
            // Note: Update URL if hosted elsewhere
            const response = await fetch('api_chat.php?action=get_active_chats');
            const data = await response.json();
            
            if (data.success && data.chats) {
                const list = document.getElementById('chat-list');
                list.innerHTML = '';
                data.chats.forEach(chat => {
                    const div = document.createElement('div');
                    div.className = 'chat-list-item';
                    div.innerHTML = `<strong>${chat.name}</strong><br><small>${chat.last_message}</small>`;
                    div.onclick = () => selectChat(chat.user_id, chat.name);
                    list.appendChild(div);
                });
            }
        } catch (error) {
            console.error('Error fetching chats:', error);
        }
    }

    function selectChat(userId, userName) {
        activeUserId = userId;
        document.getElementById('chat-title').innerText = 'Chat with ' + userName;
        document.getElementById('message-input').disabled = false;
        document.getElementById('send-btn').disabled = false;
        document.getElementById('chatbot-toggle-btn').disabled = false;
        
        // Initial load
        fetchMessages();
        
        // Start polling function
        if (autoRefreshTimer) {
            clearInterval(autoRefreshTimer);
        }
        autoRefreshTimer = setInterval(fetchMessages, 3000);
    }

    async function fetchMessages() {
        if (!activeUserId) return;
        
        try {
            const response = await fetch(`api_chat.php?action=get_messages&user_id=${activeUserId}`);
            const data = await response.json();
            
            if (data.success && data.messages) {
                const msgsContainer = document.getElementById('messages');
                msgsContainer.innerHTML = '';
                
                data.messages.forEach(msg => {
                    const div = document.createElement('div');
                    const isAdmin = parseInt(msg.sender_id) === 1; // Assuming 1 is Admin ID
                    div.className = 'message ' + (isAdmin ? 'admin-message' : 'user-message');
                    div.innerText = msg.message;
                    msgsContainer.appendChild(div);
                });
                msgsContainer.scrollTop = msgsContainer.scrollHeight;
            }
        } catch (error) {
            console.error('Error fetching messages:', error);
        }
    }

    async function sendMessage() {
        const input = document.getElementById('message-input');
        const text = input.value.trim();
        if (!text || !activeUserId) return;

        input.value = '';
        
        try {
            // Using POST to send action
            await fetch(`api_chat.php?action=send_message`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    sender_id: 1, // Admin ID
                    receiver_id: activeUserId,
                    message: text
                })
            });
            fetchMessages(); // refresh immediately after sending
        } catch (error) {
            console.error('Error sending message:', error);
        }
    }
    
    function autoReply() {
        const input = document.getElementById('message-input');
        input.value = "Generating... ✨";
        setTimeout(() => {
            input.value = "Thank you for reaching out! I'll look into it right away.";
        }, 1000); // Simulate bot delay
    }

    // Attach enter key for sending
    document.getElementById('message-input').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') sendMessage();
    });

    // Initialize list
    loadActiveChats();
</script>
</body>
</html>
