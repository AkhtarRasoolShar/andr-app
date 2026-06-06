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
    
    $email = $data['email'] ?? '';
    $pass = $data['password'] ?? '';
    
    if (empty($email) || empty($pass)) {
        echo json_encode(["success" => false, "message" => "Email and password are required.", "user" => null]);
        exit;
    }
    
    // Check credentials
    $stmt = $conn->prepare("SELECT id, name, email, role, password FROM users WHERE email = ? LIMIT 1");
    $stmt->execute([$email]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if ($user && password_verify($pass, $user['password'])) {
        // Remove password hash from response
        unset($user['password']);
        
        echo json_encode([
            "success" => true,
            "message" => "Login successful",
            "user" => $user
        ]);
    } else {
        echo json_encode(["success" => false, "message" => "Invalid email or password.", "user" => null]);
    }
} catch(PDOException $e) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Database Connection Failed: " . $e->getMessage()]);
    exit();
}
?>
