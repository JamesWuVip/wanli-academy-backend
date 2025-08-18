const axios = require('axios');

// 配置axios默认设置
axios.defaults.timeout = 10000;

async function testPermissionDebug() {
    console.log('=== 开始权限调试测试 ===');
    
    try {
        // 1. 学生登录
        console.log('\n1. 学生登录...');
        const loginResponse = await axios.post('http://localhost:8080/api/auth/login', {
            usernameOrEmail: 'test_student1',
            password: 'password123'
        });
        
        console.log('登录响应:', JSON.stringify(loginResponse.data, null, 2));
        
        const token = loginResponse.data.data.accessToken;
        console.log('获取到token:', token.substring(0, 50) + '...');
        
        // 2. 设置认证头
        const authHeaders = {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        };
        
        // 3. 测试获取当前用户信息
        console.log('\n2. 测试获取当前用户信息...');
        try {
            const userResponse = await axios.get('http://localhost:8080/api/users/me', {
                headers: authHeaders
            });
            console.log('用户信息响应:', JSON.stringify(userResponse.data, null, 2));
        } catch (error) {
            console.log('获取用户信息失败:', error.response?.status, error.response?.data);
        }
        
        // 4. 测试访问作业详情
        console.log('\n3. 测试访问作业详情...');
        const assignmentId = '660e8400-e29b-41d4-a716-446655440104';
        
        try {
            const assignmentResponse = await axios.get(`http://localhost:8080/api/assignments/${assignmentId}`, {
                headers: authHeaders
            });
            console.log('作业详情响应:', JSON.stringify(assignmentResponse.data, null, 2));
        } catch (error) {
            console.log('访问作业详情失败:');
            console.log('状态码:', error.response?.status);
            console.log('错误数据:', JSON.stringify(error.response?.data, null, 2));
            console.log('请求头:', JSON.stringify(authHeaders, null, 2));
        }
        
    } catch (error) {
        console.error('测试过程中发生错误:', error.message);
        if (error.response) {
            console.error('错误响应:', error.response.data);
        }
    }
}

testPermissionDebug();