<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-API-KEY');

// Sample mock connection
// $conn = new mysqli($host, $user, $pass, $db);

$action = $_GET['action'] ?? '';

if ($action === 'get_active_chats') {
    // Requires a chat_messages table and users table join
    // This query fetches the latest message per user interacting with admin
    $query = "
        SELECT 
            u.id as user_id, 
            u.name, 
            u.email, 
            cm.message as last_message, 
            cm.created_at as last_message_time
        FROM users u
        INNER JOIN (
            SELECT sender_id, receiver_id, message, created_at
            FROM chat_messages
            WHERE id IN (
                SELECT MAX(id)
                FROM chat_messages
                GROUP BY CASE WHEN sender_id = 1 THEN receiver_id ELSE sender_id END
            )
        ) cm ON (u.id = cm.sender_id OR u.id = cm.receiver_id)
        WHERE u.id != 1
        GROUP BY u.id
        ORDER BY cm.created_at DESC
    ";
    
    // Mock Response assuming the query runs successfully
    echo json_encode([
        'success' => true,
        'chats' => [
            [
                'user_id' => 2,
                'name' => 'John Doe',
                'email' => 'john@example.com',
                'last_message' => 'Is this available?',
                'last_message_time' => '2026-06-06 10:45:00'
            ]
        ]
    ]);
    exit;
} elseif ($action === 'get_messages') {
    $userId = $_GET['user_id'] ?? 0;
    $otherId = $_GET['other_id'] ?? 0;
    
    echo json_encode([
        'success' => true,
        'messages' => []
    ]);
    exit;
}
?>
