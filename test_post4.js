const http = require('https');
['get_users', 'get_active_chats', 'get_admin_chats', 'get_conversations', 'get_chatters', 'list_users'].forEach(action => {
  const options = {
    hostname: 'akhtarhussain.site',
    path: '/api/api_chat.php?action=' + action,
    method: 'GET'
  };
  const req = http.request(options, res => {
    let body = '';
    res.on('data', d => body += d);
    res.on('end', () => console.log('Action:', action, 'Response:', body.substring(0, 100)));
  });
  req.end();
});
