const axios = require('axios');

// 测试管理员登录的调试脚本
async function debugAdminLogin() {
    const baseURL = 'http://localhost:8080';
    const client = axios.create({
        baseURL,
        timeout: 5000,
        headers: {
            'Content-Type': 'application/json'
        }
    });

    console.log('🔍 开始调试管理员登录...');
    
    try {
        // 测试管理员登录
        const loginData = {
            usernameOrEmail: 'test_admin',
            password: 'password123'
        };
        
        console.log('📤 发送登录请求:', JSON.stringify(loginData, null, 2));
        
        const response = await client.post('/api/auth/login', loginData);
        
        console.log('📥 登录响应状态:', response.status);
        console.log('📥 登录响应数据:', JSON.stringify(response.data, null, 2));
        
        if (response.status === 200 && response.data && response.data.success && response.data.data && response.data.data.accessToken) {
            console.log('✅ 管理员登录成功!');
            console.log('🔑 访问令牌:', response.data.data.accessToken.substring(0, 50) + '...');
            return true;
        } else {
            console.log('❌ 管理员登录失败 - 响应格式不正确');
            return false;
        }
        
    } catch (error) {
        console.log('❌ 管理员登录异常:', error.message);
        if (error.response) {
            console.log('📥 错误响应状态:', error.response.status);
            console.log('📥 错误响应数据:', JSON.stringify(error.response.data, null, 2));
        }
        return false;
    }
}

// 运行调试
debugAdminLogin().then(success => {
    console.log('\n' + '='.repeat(50));
    console.log('🎯 调试结果:', success ? '成功' : '失败');
    process.exit(success ? 0 : 1);
}).catch(error => {
    console.error('💥 调试脚本异常:', error);
    process.exit(1);
});