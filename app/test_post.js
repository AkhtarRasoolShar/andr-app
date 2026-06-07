const http = require('https');
['get_users', 'get_active_chats', 'get_all_users', 'get_chatters', 'list_users', 'get_chat_users'].forEach(action => {
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
