const http = require('http');

const postData = JSON.stringify({
  usernameOrEmail: 'test_admin',
  password: 'password123'
});

const options = {
  hostname: 'localhost',
  port: 8080,
  path: '/api/auth/login',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Content-Length': Buffer.byteLength(postData)
  }
};

const req = http.request(options, (res) => {
  console.log(`状态码: ${res.statusCode}`);
  console.log(`响应头: ${JSON.stringify(res.headers)}`);
  
  let data = '';
  res.on('data', (chunk) => {
    data += chunk;
  });
  
  res.on('end', () => {
    console.log('响应体:');
    console.log(data);
    
    if (res.statusCode === 200) {
      try {
        const response = JSON.parse(data);
        console.log('\n登录成功!');
        const authData = response.data || response;
        console.log('访问令牌:', authData.accessToken ? authData.accessToken.substring(0, 50) + '...' : '未找到');
        console.log('用户ID:', authData.userId);
        console.log('用户名:', authData.username);
        console.log('角色:', authData.roles);
        
        // 保存令牌用于后续测试
        if (authData.accessToken) {
          console.log('\n完整访问令牌:', authData.accessToken);
        }
      } catch (e) {
        console.log('解析JSON失败:', e.message);
      }
    } else {
      console.log('\n登录失败，状态码:', res.statusCode);
    }
  });
});

req.on('error', (e) => {
  console.error(`请求错误: ${e.message}`);
});

req.write(postData);
req.end();