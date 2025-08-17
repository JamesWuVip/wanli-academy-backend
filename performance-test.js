const axios = require('axios');
const fs = require('fs');

// 配置
const BASE_URL = 'http://localhost:8080';
const CONCURRENT_USERS = 100;
const MAX_RESPONSE_TIME = 2000; // 2秒

// 测试用户凭据 - 使用实际的测试用户
const TEST_CREDENTIALS = {
    username: 'testuser',
    password: 'password123'
};

// 性能测试结果
let performanceResults = {
    timestamp: new Date().toISOString(),
    totalTests: 0,
    passedTests: 0,
    failedTests: 0,
    averageResponseTime: 0,
    maxResponseTime: 0,
    minResponseTime: Infinity,
    concurrentUserTest: {
        success: false,
        completedRequests: 0,
        failedRequests: 0,
        averageResponseTime: 0
    },
    apiTests: []
};

// 获取认证令牌
async function getAuthToken() {
    try {
        const response = await axios.post(`${BASE_URL}/api/auth/login`, TEST_CREDENTIALS);
        return response.data.token;
    } catch (error) {
        console.log('警告: 无法获取认证令牌，将进行无认证测试');
        return null;
    }
}

// 测试单个API端点
async function testApiEndpoint(endpoint, method = 'GET', data = null, token = null) {
    const startTime = Date.now();
    const headers = {};
    
    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }
    
    try {
        let response;
        switch (method.toUpperCase()) {
            case 'GET':
                response = await axios.get(`${BASE_URL}${endpoint}`, { headers });
                break;
            case 'POST':
                response = await axios.post(`${BASE_URL}${endpoint}`, data, { headers });
                break;
            case 'PUT':
                response = await axios.put(`${BASE_URL}${endpoint}`, data, { headers });
                break;
            case 'DELETE':
                response = await axios.delete(`${BASE_URL}${endpoint}`, { headers });
                break;
            default:
                throw new Error(`不支持的HTTP方法: ${method}`);
        }
        
        const responseTime = Date.now() - startTime;
        const success = response.status >= 200 && response.status < 300 && responseTime <= MAX_RESPONSE_TIME;
        
        return {
            endpoint,
            method,
            status: response.status,
            responseTime,
            success,
            error: null
        };
    } catch (error) {
        const responseTime = Date.now() - startTime;
        return {
            endpoint,
            method,
            status: error.response?.status || 0,
            responseTime,
            success: false,
            error: error.message
        };
    }
}

// 并发用户测试
async function concurrentUserTest(token) {
    console.log(`\n开始并发用户测试 (${CONCURRENT_USERS} 个并发用户)...`);
    
    const promises = [];
    const testEndpoint = '/actuator/health'; // 使用健康检查API进行并发测试
    
    for (let i = 0; i < CONCURRENT_USERS; i++) {
        promises.push(testApiEndpoint(testEndpoint, 'GET', null, token));
    }
    
    const startTime = Date.now();
    const results = await Promise.allSettled(promises);
    const totalTime = Date.now() - startTime;
    
    let completedRequests = 0;
    let failedRequests = 0;
    let totalResponseTime = 0;
    
    results.forEach(result => {
        if (result.status === 'fulfilled' && result.value.success) {
            completedRequests++;
            totalResponseTime += result.value.responseTime;
        } else {
            failedRequests++;
        }
    });
    
    const averageResponseTime = completedRequests > 0 ? totalResponseTime / completedRequests : 0;
    const successRate = (completedRequests / CONCURRENT_USERS) * 100;
    
    performanceResults.concurrentUserTest = {
        success: successRate >= 90, // 90%以上成功率认为通过
        completedRequests,
        failedRequests,
        averageResponseTime,
        totalTime,
        successRate
    };
    
    console.log(`并发测试完成: ${completedRequests}/${CONCURRENT_USERS} 成功, 平均响应时间: ${averageResponseTime.toFixed(2)}ms`);
}

// 主要性能测试
async function runPerformanceTests() {
    console.log('开始性能测试...');
    console.log(`目标: API响应时间 < ${MAX_RESPONSE_TIME}ms, 支持 ${CONCURRENT_USERS} 并发用户\n`);
    
    // 获取认证令牌
    const token = await getAuthToken();
    
    // 定义要测试的API端点 - 主要测试公开端点和健康检查
    const apiEndpoints = [
        { endpoint: '/swagger-ui/index.html', method: 'GET', requireAuth: false },
        { endpoint: '/actuator/health', method: 'GET', requireAuth: false },
        { endpoint: '/actuator/info', method: 'GET', requireAuth: false },
        { endpoint: '/v3/api-docs', method: 'GET', requireAuth: false },
        { endpoint: '/api/assignments', method: 'GET', requireAuth: true },
        { endpoint: '/api/submissions', method: 'GET', requireAuth: true }
    ];
    
    console.log('测试各个API端点响应时间...');
    
    let totalResponseTime = 0;
    let validTests = 0;
    
    for (const api of apiEndpoints) {
        const useToken = api.requireAuth ? token : null;
        const result = await testApiEndpoint(api.endpoint, api.method, api.data, useToken);
        
        performanceResults.apiTests.push(result);
        performanceResults.totalTests++;
        
        if (result.success) {
            performanceResults.passedTests++;
            totalResponseTime += result.responseTime;
            validTests++;
            
            // 更新最大和最小响应时间
            performanceResults.maxResponseTime = Math.max(performanceResults.maxResponseTime, result.responseTime);
            performanceResults.minResponseTime = Math.min(performanceResults.minResponseTime, result.responseTime);
        } else {
            performanceResults.failedTests++;
        }
        
        const statusIcon = result.success ? '✅' : '❌';
        const authInfo = api.requireAuth ? '(需要认证)' : '(无需认证)';
        console.log(`${statusIcon} ${api.method} ${api.endpoint} ${authInfo} - ${result.responseTime}ms - Status: ${result.status}`);
        
        if (result.error) {
            console.log(`   错误: ${result.error}`);
        }
    }
    
    // 计算平均响应时间
    performanceResults.averageResponseTime = validTests > 0 ? totalResponseTime / validTests : 0;
    
    // 运行并发用户测试
    await concurrentUserTest(token);
    
    // 生成测试报告
    generatePerformanceReport();
}

// 生成性能测试报告
function generatePerformanceReport() {
    console.log('\n' + '='.repeat(60));
    console.log('性能测试报告');
    console.log('='.repeat(60));
    
    console.log(`测试时间: ${performanceResults.timestamp}`);
    console.log(`总测试数: ${performanceResults.totalTests}`);
    console.log(`通过测试: ${performanceResults.passedTests}`);
    console.log(`失败测试: ${performanceResults.failedTests}`);
    console.log(`成功率: ${((performanceResults.passedTests / performanceResults.totalTests) * 100).toFixed(2)}%`);
    
    console.log('\n响应时间统计:');
    console.log(`平均响应时间: ${performanceResults.averageResponseTime.toFixed(2)}ms`);
    console.log(`最大响应时间: ${performanceResults.maxResponseTime}ms`);
    console.log(`最小响应时间: ${performanceResults.minResponseTime === Infinity ? 'N/A' : performanceResults.minResponseTime + 'ms'}`);
    
    console.log('\n并发用户测试结果:');
    const concurrentTest = performanceResults.concurrentUserTest;
    console.log(`测试状态: ${concurrentTest.success ? '✅ 通过' : '❌ 失败'}`);
    console.log(`完成请求: ${concurrentTest.completedRequests}/${CONCURRENT_USERS}`);
    console.log(`失败请求: ${concurrentTest.failedRequests}`);
    console.log(`成功率: ${concurrentTest.successRate.toFixed(2)}%`);
    console.log(`平均响应时间: ${concurrentTest.averageResponseTime.toFixed(2)}ms`);
    console.log(`总耗时: ${concurrentTest.totalTime}ms`);
    
    // 性能要求验证
    console.log('\n性能要求验证:');
    const responseTimeOk = performanceResults.averageResponseTime <= MAX_RESPONSE_TIME;
    const concurrentOk = concurrentTest.success;
    
    console.log(`✓ API响应时间 < ${MAX_RESPONSE_TIME}ms: ${responseTimeOk ? '✅ 通过' : '❌ 失败'}`);
    console.log(`✓ 支持${CONCURRENT_USERS}并发用户: ${concurrentOk ? '✅ 通过' : '❌ 失败'}`);
    
    // 计算公开端点的成功率（排除需要认证的端点）
    const publicEndpointTests = performanceResults.apiTests.filter(test => 
        test.endpoint.includes('/swagger-ui/') || 
        test.endpoint.includes('/actuator/health') || 
        test.endpoint.includes('/v3/api-docs')
    );
    const publicSuccessRate = publicEndpointTests.length > 0 ? 
        publicEndpointTests.filter(test => test.success).length / publicEndpointTests.length : 0;
    
    const overallSuccess = responseTimeOk && concurrentOk && publicSuccessRate >= 0.8;
    console.log(`\n总体评估: ${overallSuccess ? '✅ 性能测试通过' : '❌ 性能测试失败'}`);
    
    // 保存详细报告到文件
    const reportData = {
        ...performanceResults,
        performanceRequirements: {
            maxResponseTime: MAX_RESPONSE_TIME,
            concurrentUsers: CONCURRENT_USERS,
            responseTimeOk,
            concurrentOk,
            overallSuccess
        }
    };
    
    fs.writeFileSync('performance-test-report.json', JSON.stringify(reportData, null, 2));
    console.log('\n详细报告已保存到: performance-test-report.json');
}

// 运行测试
if (require.main === module) {
    runPerformanceTests().catch(error => {
        console.error('性能测试执行失败:', error);
        process.exit(1);
    });
}

module.exports = { runPerformanceTests };