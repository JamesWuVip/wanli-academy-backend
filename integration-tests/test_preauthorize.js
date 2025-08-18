const axios = require('axios');

// 配置
const BASE_URL = 'http://localhost:8080';
const STUDENT_USERNAME = 'test_student1';
const STUDENT_PASSWORD = 'password123';
const TEST_ASSIGNMENT_ID = '660e8400-e29b-41d4-a716-446655440104';

async function testPreAuthorize() {
    console.log('=== 测试@PreAuthorize注解是否正常工作 ===\n');
    
    try {
        // 1. 学生用户登录
        console.log('1. 学生用户登录...');
        const loginResponse = await axios.post(`${BASE_URL}/api/auth/login`, {
            usernameOrEmail: STUDENT_USERNAME,
            password: STUDENT_PASSWORD
        });
        
        const token = loginResponse.data.data?.accessToken || loginResponse.data.token;
        console.log('✓ 登录成功，获取到JWT Token');
        console.log('Token前缀:', token ? token.substring(0, 50) + '...' : 'Token为空');
        
        if (!token) {
            console.error('未能获取到有效的JWT Token');
            console.log('登录响应:', JSON.stringify(loginResponse.data, null, 2));
            return;
        }
        
        const headers = {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        };
        
        // 2. 测试无权限控制的端点
        console.log('\n2. 测试无权限控制的端点...');
        try {
            const noPermResponse = await axios.get(`${BASE_URL}/api/test/no-permission`, { headers });
            console.log('✓ 无权限控制端点访问成功:', noPermResponse.data);
        } catch (error) {
            console.log('✗ 无权限控制端点访问失败:', error.response?.status, error.response?.data?.message || error.message);
        }
        
        // 3. 测试有@PreAuthorize注解的端点
        console.log('\n3. 测试有@PreAuthorize注解的端点...');
        try {
            const permResponse = await axios.get(`${BASE_URL}/api/test/permission/${TEST_ASSIGNMENT_ID}`, { headers });
            console.log('✓ @PreAuthorize端点访问成功:', permResponse.data);
        } catch (error) {
            console.log('✗ @PreAuthorize端点访问失败:', error.response?.status, error.response?.data?.message || error.message);
        }
        
        // 4. 测试原始的作业详情端点
        console.log('\n4. 测试原始的作业详情端点...');
        try {
            const assignmentResponse = await axios.get(`${BASE_URL}/api/assignments/${TEST_ASSIGNMENT_ID}`, { headers });
            console.log('✓ 作业详情端点访问成功:', assignmentResponse.data.title);
        } catch (error) {
            console.log('✗ 作业详情端点访问失败:', error.response?.status, error.response?.data?.message || error.message);
        }
        
    } catch (error) {
        console.error('测试过程中发生错误:', error.message);
        if (error.response) {
            console.error('响应状态:', error.response.status);
            console.error('响应数据:', error.response.data);
        }
    }
}

// 运行测试
testPreAuthorize().then(() => {
    console.log('\n=== 测试完成 ===');
}).catch(error => {
    console.error('测试失败:', error.message);
});