const http = require('http');

// 使用刚获取的访问令牌
const accessToken = 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6InRlc3RfYWRtaW4iLCJpYXQiOjE3NTU0MjgxMDUsImV4cCI6MTc1NTUxNDUwNX0.7VqOgjA4w7mQI51Q5CpNx4waZVrJOkVmzhvxkkpNxsM';

const options = {
  hostname: 'localhost',
  port: 8080,
  path: '/api/users',
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
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
        console.log('\n用户列表API测试成功!');
        const users = response.data || response;
        console.log(`获取到 ${Array.isArray(users) ? users.length : '未知数量'} 个用户`);
        if (Array.isArray(users) && users.length > 0) {
          console.log('第一个用户:', {
            id: users[0].id,
            username: users[0].username,
            email: users[0].email,
            roles: users[0].roles
          });
        }
      } catch (e) {
        console.log('解析JSON失败:', e.message);
      }
    } else {
      console.log('\n用户列表API测试失败，状态码:', res.statusCode);
      if (res.statusCode === 500) {
        console.log('仍然存在500错误，需要进一步调试');
      } else if (res.statusCode === 401) {
        console.log('认证失败，令牌可能无效或已过期');
      } else if (res.statusCode === 403) {
        console.log('权限不足，用户可能没有访问权限');
      }
    }
  });
});

req.on('error', (e) => {
  console.error(`请求错误: ${e.message}`);
});

req.end();