#!/usr/bin/env node

/**
 * Sprint 1 自动化验收测试脚本
 * 
 * 功能:
 * 1. 创建随机新用户确保测试可重复
 * 2. 尝试访问受保护资源（预期失败）
 * 3. 使用新用户凭证登录并获取认证Token
 * 4. 使用Token成功访问受保护资源并验证返回信息
 */

const axios = require('axios');
const crypto = require('crypto');

// 配置外部化 - 支持从环境变量读取测试目标URL
// 使用方法:
// 本地开发: node acceptance-test.js (默认使用 http://localhost:8080/api)
// CI/CD环境: TEST_BASE_URL=https://wanli-staging.fly.dev/api node acceptance-test.js
// 或者: export TEST_BASE_URL=https://wanli-staging.fly.dev/api && node acceptance-test.js
const BASE_URL = process.env.TEST_BASE_URL || 'http://localhost:8080/api';

// 输出当前使用的测试环境URL，方便调试
console.log(`🌐 测试环境: ${BASE_URL}`);

// 配置
const CONFIG = {
    BASE_URL: BASE_URL,
    TIMEOUT: 10000,
    RETRY_COUNT: 3,
    RETRY_DELAY: 1000
};

// 测试结果统计

// 测试结果统计
const testResults = {
    total: 0,
    passed: 0,
    failed: 0,
    errors: []
};

// 颜色输出
const colors = {
    green: '\x1b[32m',
    red: '\x1b[31m',
    yellow: '\x1b[33m',
    blue: '\x1b[34m',
    reset: '\x1b[0m',
    bold: '\x1b[1m'
};

/**
 * 日志输出函数
 */
function log(message, color = colors.reset) {
    console.log(`${color}${message}${colors.reset}`);
}

function logSuccess(message) {
    log(`✓ ${message}`, colors.green);
}

function logError(message) {
    log(`✗ ${message}`, colors.red);
}

function logInfo(message) {
    log(`ℹ ${message}`, colors.blue);
}

function logWarning(message) {
    log(`⚠ ${message}`, colors.yellow);
}

/**
 * 生成随机用户数据
 */
function generateRandomUser() {
    const timestamp = Date.now();
    const randomId = crypto.randomBytes(4).toString('hex');
    
    return {
        username: `testuser_${timestamp}_${randomId}`,
        email: `test_${timestamp}_${randomId}@example.com`,
        password: 'TestPassword123!',
        firstName: 'Test',
        lastName: 'User',
        phoneNumber: '1234567890'
    };
}

/**
 * HTTP请求封装函数
 */
async function makeRequest(method, url, data = null, headers = {}) {
    const config = {
        method,
        url: `${CONFIG.BASE_URL}${url}`,
        timeout: CONFIG.TIMEOUT,
        headers: {
            'Content-Type': 'application/json',
            ...headers
        }
    };
    
    if (data) {
        config.data = data;
    }
    
    try {
        const response = await axios(config);
        return {
            success: true,
            status: response.status,
            data: response.data,
            headers: response.headers
        };
    } catch (error) {
        return {
            success: false,
            status: error.response?.status || 0,
            data: error.response?.data || null,
            error: error.message,
            headers: error.response?.headers || {}
        };
    }
}

/**
 * 等待函数
 */
function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

/**
 * 测试断言函数
 */
function assert(condition, message) {
    testResults.total++;
    if (condition) {
        testResults.passed++;
        logSuccess(message);
        return true;
    } else {
        testResults.failed++;
        const error = `断言失败: ${message}`;
        testResults.errors.push(error);
        logError(error);
        return false;
    }
}

/**
 * 检查服务器是否运行
 */
async function checkServerHealth() {
    logInfo('检查服务器健康状态...');
    
    const response = await makeRequest('GET', '/auth/health');
    
    if (response.success && response.status === 200) {
        logSuccess('服务器运行正常');
        return true;
    } else {
        logError(`服务器健康检查失败: ${response.error || '未知错误'}`);
        return false;
    }
}

/**
 * 测试1: 创建随机新用户
 */
async function testUserRegistration(userData) {
    logInfo('\n=== 测试1: 用户注册 ===');
    
    const response = await makeRequest('POST', '/auth/register', userData);
    
    // 验证注册成功
    assert(response.success, '用户注册请求成功');
    assert(response.status === 201, `注册返回状态码为201，实际: ${response.status}`);
    assert(response.data && response.data.success === true, '注册响应success字段为true');
    assert(response.data && response.data.data, '注册响应包含data字段');
    assert(response.data && response.data.data.accessToken, '注册响应包含accessToken');
    assert(response.data && response.data.data.refreshToken, '注册响应包含refreshToken');
    
    if (response.success && response.data && response.data.data) {
        logSuccess(`用户注册成功: ${userData.username}`);
        return {
            accessToken: response.data.data.accessToken,
            refreshToken: response.data.data.refreshToken,
            user: response.data.data.user
        };
    } else {
        logError(`用户注册失败: ${response.error || JSON.stringify(response.data)}`);
        return null;
    }
}

/**
 * 测试2: 未授权访问受保护资源（预期失败）
 */
async function testUnauthorizedAccess() {
    logInfo('\n=== 测试2: 未授权访问受保护资源 ===');
    
    // 尝试访问用户信息端点（不提供Token）
    const response = await makeRequest('GET', '/users/me');
    
    // 验证访问被拒绝
    assert(!response.success, '未授权访问被正确拒绝');
    assert(response.status === 401 || response.status === 403, 
           `返回正确的未授权状态码(401/403)，实际: ${response.status}`);
    
    logSuccess('未授权访问测试通过 - 系统正确拒绝了未认证请求');
}

/**
 * 测试3: 用户登录获取Token
 */
async function testUserLogin(userData) {
    logInfo('\n=== 测试3: 用户登录 ===');
    
    const loginData = {
        usernameOrEmail: userData.username,
        password: userData.password
    };
    
    const response = await makeRequest('POST', '/auth/login', loginData);
    
    // 验证登录成功
    assert(response.success, '用户登录请求成功');
    assert(response.status === 200, `登录返回状态码为200，实际: ${response.status}`);
    assert(response.data && response.data.success === true, '登录响应success字段为true');
    assert(response.data && response.data.data, '登录响应包含data字段');
    assert(response.data && response.data.data.accessToken, '登录响应包含accessToken');
    assert(response.data && response.data.data.refreshToken, '登录响应包含refreshToken');
    
    if (response.success && response.data && response.data.data) {
        logSuccess(`用户登录成功: ${userData.username}`);
        return {
            accessToken: response.data.data.accessToken,
            refreshToken: response.data.data.refreshToken,
            user: response.data.data.user
        };
    } else {
        logError(`用户登录失败: ${response.error || JSON.stringify(response.data)}`);
        return null;
    }
}

/**
 * 测试4: 使用Token访问受保护资源
 */
async function testAuthorizedAccess(accessToken, expectedUser) {
    logInfo('\n=== 测试4: 使用Token访问受保护资源 ===');
    
    const headers = {
        'Authorization': `Bearer ${accessToken}`
    };
    
    // 测试获取当前用户信息
    const response = await makeRequest('GET', '/users/me', null, headers);
    
    // 验证访问成功
    assert(response.success, '使用Token访问受保护资源成功');
    assert(response.status === 200, `返回状态码为200，实际: ${response.status}`);
    assert(response.data && response.data.success === true, '响应success字段为true');
    assert(response.data && response.data.data, '响应包含data字段');
    
    if (response.success && response.data && response.data.data) {
        const userData = response.data.data;
        
        // 验证返回的用户信息
        assert(userData.username === expectedUser.username, 
               `用户名匹配，期望: ${expectedUser.username}，实际: ${userData.username}`);
        assert(userData.email === expectedUser.email, 
               `邮箱匹配，期望: ${expectedUser.email}，实际: ${userData.email}`);
        assert(userData.isActive === true, '用户状态为激活');
        assert(Array.isArray(userData.roles) && userData.roles.length > 0, '用户包含角色信息');
        
        logSuccess('受保护资源访问测试通过 - 成功获取用户信息');
        logInfo(`获取到的用户信息: ID=${userData.id}, 用户名=${userData.username}, 邮箱=${userData.email}`);
        
        return userData;
    } else {
        logError(`访问受保护资源失败: ${response.error || JSON.stringify(response.data)}`);
        return null;
    }
}

/**
 * 测试5: 验证认证状态
 */
async function testAuthStatus(accessToken) {
    logInfo('\n=== 测试5: 验证认证状态 ===');
    
    const headers = {
        'Authorization': `Bearer ${accessToken}`
    };
    
    const response = await makeRequest('GET', '/users/auth-status', null, headers);
    
    // 验证认证状态
    assert(response.success, '认证状态检查请求成功');
    assert(response.status === 200, `返回状态码为200，实际: ${response.status}`);
    assert(response.data && response.data.success === true, '响应success字段为true');
    assert(response.data && response.data.authenticated === true, '认证状态为true');
    
    if (response.success && response.data) {
        logSuccess('认证状态验证通过');
        logInfo(`认证状态: ${response.data.authenticated ? '已认证' : '未认证'}`);
        return true;
    } else {
        logError(`认证状态检查失败: ${response.error || JSON.stringify(response.data)}`);
        return false;
    }
}

/**
 * 打印测试结果摘要
 */
function printTestSummary() {
    log('\n' + '='.repeat(60), colors.bold);
    log('测试结果摘要', colors.bold);
    log('='.repeat(60), colors.bold);
    
    log(`总测试数: ${testResults.total}`);
    logSuccess(`通过: ${testResults.passed}`);
    
    if (testResults.failed > 0) {
        logError(`失败: ${testResults.failed}`);
        log('\n失败详情:', colors.red);
        testResults.errors.forEach((error, index) => {
            log(`${index + 1}. ${error}`, colors.red);
        });
    }
    
    const successRate = testResults.total > 0 ? 
        ((testResults.passed / testResults.total) * 100).toFixed(2) : 0;
    
    log(`\n成功率: ${successRate}%`, 
        successRate === '100.00' ? colors.green : colors.yellow);
    
    if (testResults.failed === 0) {
        logSuccess('\n🎉 所有验收测试通过！Sprint 1 认证功能验收成功！');
    } else {
        logError('\n❌ 部分测试失败，请检查上述错误信息');
    }
    
    log('='.repeat(60), colors.bold);
}

/**
 * 主测试函数
 */
async function runAcceptanceTests() {
    log('🚀 开始执行 Sprint 1 自动化验收测试', colors.bold);
    log('测试目标: 验证用户认证和授权功能', colors.blue);
    log('='.repeat(60));
    
    try {
        // 检查服务器健康状态
        const serverHealthy = await checkServerHealth();
        if (!serverHealthy) {
            logError('服务器不可用，测试终止');
            process.exit(1);
        }
        
        // 生成随机用户数据
        const userData = generateRandomUser();
        logInfo(`生成测试用户: ${userData.username} (${userData.email})`);
        
        // 执行测试序列
        const registrationResult = await testUserRegistration(userData);
        if (!registrationResult) {
            logError('用户注册失败，后续测试无法继续');
            return;
        }
        
        await testUnauthorizedAccess();
        
        const loginResult = await testUserLogin(userData);
        if (!loginResult) {
            logError('用户登录失败，后续测试无法继续');
            return;
        }
        
        const userInfo = await testAuthorizedAccess(loginResult.accessToken, userData);
        if (!userInfo) {
            logError('访问受保护资源失败');
            return;
        }
        
        await testAuthStatus(loginResult.accessToken);
        
        logSuccess('\n✅ 所有核心测试流程完成');
        
    } catch (error) {
        logError(`测试执行过程中发生未预期错误: ${error.message}`);
        testResults.errors.push(`未预期错误: ${error.message}`);
    } finally {
        printTestSummary();
    }
}

/**
 * 程序入口
 */
if (require.main === module) {
    // 处理未捕获的异常
    process.on('unhandledRejection', (reason, promise) => {
        logError(`未处理的Promise拒绝: ${reason}`);
        process.exit(1);
    });
    
    process.on('uncaughtException', (error) => {
        logError(`未捕获的异常: ${error.message}`);
        process.exit(1);
    });
    
    // 运行测试
    runAcceptanceTests()
        .then(() => {
            const exitCode = testResults.failed > 0 ? 1 : 0;
            process.exit(exitCode);
        })
        .catch((error) => {
            logError(`测试运行失败: ${error.message}`);
            process.exit(1);
        });
}

module.exports = {
    runAcceptanceTests,
    testResults,
    CONFIG
};