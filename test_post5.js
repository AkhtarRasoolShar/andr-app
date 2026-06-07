const http = require('https');
  const options = {
    hostname: 'akhtarhussain.site',
    path: '/api/api_chat.php?action=get_conversations',
    method: 'GET'
  };
  const req = http.request(options, res => {
    let body = '';
    res.on('data', d => body += d);
    res.on('end', () => console.log(body));
  });
  req.end();
