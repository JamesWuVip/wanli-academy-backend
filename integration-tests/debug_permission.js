const axios = require('axios');
const jwt = require('jsonwebtoken');

// 配置
const BASE_URL = 'http://localhost:8080';
const STUDENT_CREDENTIALS = {
    usernameOrEmail: 'test_student1',
    password: 'password123'
};

// 测试的作业ID（来自测试数据）
const ASSIGNMENT_IDS = [
    '660e8400-e29b-41d4-a716-446655440104',
    '660e8400-e29b-41d4-a716-446655440101'
];

async function debugPermission() {
    console.log('=== 权限调试脚本开始 ===\n');
    
    try {
        // 1. 学生登录
        console.log('1. 学生用户登录...');
        const loginResponse = await axios.post(`${BASE_URL}/api/auth/login`, STUDENT_CREDENTIALS);
        console.log('登录响应结构:', JSON.stringify(loginResponse.data, null, 2));
        
        // 根据实际响应结构获取token
        const token = loginResponse.data.data?.accessToken || loginResponse.data.token || loginResponse.data.accessToken;
        if (!token) {
            throw new Error('无法从登录响应中获取token');
        }
        console.log('✓ 登录成功');
        console.log('Token:', token.substring(0, 50) + '...');
        
        // 2. 解析JWT token
        console.log('\n2. 解析JWT Token...');
        const decodedToken = jwt.decode(token);
        console.log('Token payload:', JSON.stringify(decodedToken, null, 2));
        
        // 3. 测试访问作业详情
        console.log('\n3. 测试访问作业详情...');
        const headers = {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        };
        
        for (const assignmentId of ASSIGNMENT_IDS) {
            console.log(`\n测试作业ID: ${assignmentId}`);
            try {
                const response = await axios.get(`${BASE_URL}/api/assignments/${assignmentId}`, { headers });
                console.log('✓ 访问成功:', response.status);
                console.log('作业标题:', response.data.title);
                console.log('作业状态:', response.data.status);
            } catch (error) {
                console.log('✗ 访问失败:', error.response?.status, error.response?.statusText);
                if (error.response?.data) {
                    console.log('错误详情:', error.response.data);
                }
            }
        }
        
        // 4. 测试用户信息接口
        console.log('\n4. 测试用户信息接口...');
        try {
            const userResponse = await axios.get(`${BASE_URL}/api/auth/me`, { headers });
            console.log('✓ 用户信息获取成功');
            console.log('用户信息:', JSON.stringify(userResponse.data, null, 2));
        } catch (error) {
            console.log('✗ 用户信息获取失败:', error.response?.status, error.response?.statusText);
        }
        
    } catch (error) {
        console.error('调试过程中发生错误:', error.message);
        if (error.response) {
            console.error('响应状态:', error.response.status);
            console.error('响应数据:', error.response.data);
        }
    }
    
    console.log('\n=== 权限调试脚本结束 ===');
}

// 运行调试
debugPermission().catch(console.error);