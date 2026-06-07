const http = require('https');
['get_users', 'get_active_chats', 'list_users'].forEach(action => {
  const data = JSON.stringify({ email: 'admin@google.com', password: 'password', action });
  const options = {
    hostname: 'akhtarhussain.site',
    path: '/api/admin_chat.php',
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Content-Length': data.length }
  };
  const req = http.request(options, res => {
    let body = '';
    res.on('data', d => body += d);
    res.on('end', () => console.log('Action:', action, 'Response:', body.substring(0, 100)));
  });
  req.write(data);
  req.end();
});
