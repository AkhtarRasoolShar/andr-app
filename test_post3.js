const http = require('https');
['fetch_users', 'get_conversations', 'get_chat_history', 'admin_chats', 'chats'].forEach(action => {
  const data = JSON.stringify({ action });
  const options = {
    hostname: 'akhtarhussain.site',
    path: '/api/api_admin_master.php',
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
