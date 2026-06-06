<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-API-KEY');

$host = "localhost";
$db_name = "u929353525_app"; 
$username = "u929353525_test"; 
$password = "ALiSain0099@"; 

try {
    $conn = new PDO("mysql:host=$host;dbname=$db_name;charset=utf8mb4", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    $input = file_get_contents('php://input');
    $data = json_decode($input, true);
    
    if (!$data) {
        $data = $_POST;
    }
    
    $name = $data['name'] ?? '';
    $email = $data['email'] ?? '';
    $pass = $data['password'] ?? '';
    // Optional parameter to register as admin vs customer
    $role = $data['role'] ?? 'customer'; 
    $fcm_token = $data['fcm_token'] ?? null;
    
    if (empty($name) || empty($email) || empty($pass)) {
        echo json_encode(["success" => false, "message" => "Name, email and password are required.", "user" => null]);
        exit;
    }
    
    // Check if email exists
    $stmt = $conn->prepare("SELECT id FROM users WHERE email = ? LIMIT 1");
    $stmt->execute([$email]);
    if ($stmt->fetch()) {
        echo json_encode(["success" => false, "message" => "Email already registered.", "user" => null]);
        exit;
    }
    
    // Hash password
    $hashed_password = password_hash($pass, PASSWORD_BCRYPT);
    
    // Insert user
    $stmt = $conn->prepare("INSERT INTO users (name, email, password, role, fcm_token) VALUES (?, ?, ?, ?, ?)");
    if ($stmt->execute([$name, $email, $hashed_password, $role, $fcm_token])) {
        $userId = $conn->lastInsertId();
        
        $user = [
            "id" => $userId,
            "name" => $name,
            "email" => $email,
            "role" => $role
        ];
        
        echo json_encode([
            "success" => true,
            "message" => "Registration successful",
            "user" => $user
        ]);
    } else {
        echo json_encode(["success" => false, "message" => "Registration failed.", "user" => null]);
    }
} catch(PDOException $e) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Database Connection Failed: " . $e->getMessage()]);
    exit();
}
?>
