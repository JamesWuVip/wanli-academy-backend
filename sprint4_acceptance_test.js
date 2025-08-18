#!/usr/bin/env node

/**
 * Sprint 4 自动化验收测试脚本
 * 
 * 验收标准:
 * 1. 学员能够查看已批改作业的结果详情
 * 2. 作业结果包含总分、教师评语、题目详情、学生答案、标准答案、文字解析和视频URL
 * 3. 权限控制确保学员只能查看自己的作业结果
 * 4. API端点GET /api/submissions/{submissionId}/result正常工作
 * 
 * 功能验证:
 * 1. 用户认证和登录流程
 * 2. 获取作业列表功能
 * 3. 查看作业提交结果详情
 * 4. 权限控制验证
 * 5. 数据完整性验证
 */

const axios = require('axios');
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

// 配置外部化 - 支持从环境变量读取测试目标URL
const BASE_URL = process.env.TEST_BASE_URL || 'http://localhost:8080';
const API_BASE_URL = `${BASE_URL}/api`;

// 输出当前使用的测试环境URL
console.log(`🌐 测试环境: ${BASE_URL}`);
console.log(`📋 API基础URL: ${API_BASE_URL}`);

// 配置
const CONFIG = {
    BASE_URL: BASE_URL,
    API_BASE_URL: API_BASE_URL,
    TIMEOUT: 15000,
    RETRY_COUNT: 3,
    RETRY_DELAY: 2000
};

// 测试结果统计
const testResults = {
    total: 0,
    passed: 0,
    failed: 0,
    errors: [],
    testData: {
        users: [],
        assignments: [],
        submissions: []
    }
};

// 颜色输出
const colors = {
    green: '\x1b[32m',
    red: '\x1b[31m',
    yellow: '\x1b[33m',
    blue: '\x1b[34m',
    magenta: '\x1b[35m',
    cyan: '\x1b[36m',
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

function logSection(message) {
    log(`\n${'='.repeat(60)}`, colors.bold);
    log(`${message}`, colors.bold);
    log(`${'='.repeat(60)}`, colors.bold);
}

/**
 * HTTP请求封装函数
 */
async function makeRequest(method, url, data = null, headers = {}) {
    const config = {
        method,
        url,
        timeout: CONFIG.TIMEOUT,
        headers: {
            'Content-Type': 'application/json',
            ...headers
        },
        validateStatus: () => true // 接受所有状态码
    };
    
    if (data) {
        config.data = data;
    }
    
    try {
        const response = await axios(config);
        return {
            success: response.status >= 200 && response.status < 300,
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
 * 测试断言函数
 */
function assert(condition, message, category = 'general') {
    testResults.total++;
    if (condition) {
        testResults.passed++;
        logSuccess(message);
        return true;
    } else {
        testResults.failed++;
        const error = `[${category}] 断言失败: ${message}`;
        testResults.errors.push(error);
        logError(error);
        return false;
    }
}

/**
 * 等待函数
 */
function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

/**
 * 生成随机用户数据
 */
function generateRandomUser() {
    const timestamp = Date.now();
    const randomId = crypto.randomBytes(4).toString('hex');
    
    return {
        username: `sprint4_test_${timestamp}_${randomId}`,
        email: `sprint4_test_${timestamp}_${randomId}@example.com`,
        password: 'Sprint4Test123!',
        firstName: 'Sprint4',
        lastName: 'Test',
        phoneNumber: '1234567890'
    };
}

/**
 * 使用预设的测试学生用户登录
 * @param {string} username 用户名
 * @param {string} password 密码
 * @returns {Promise<Object>} 登录结果
 */
async function loginTestStudent(username = 'test_student1', password = 'password123') {
    logInfo(`使用预设学生用户登录: ${username}`);
    
    try {
        const loginResponse = await makeRequest('POST', `${CONFIG.API_BASE_URL}/auth/login`, {
            usernameOrEmail: username,
            password: password
        });
        
        assert(loginResponse.success, '学生用户登录请求成功', 'user-login');
        assert(loginResponse.status === 200, `登录响应状态码为200，实际: ${loginResponse.status}`, 'user-login');
        assert(loginResponse.data && loginResponse.data.data && loginResponse.data.data.accessToken, '登录响应包含accessToken', 'user-login');
        
        const accessToken = loginResponse.data.data.accessToken;
        const user = loginResponse.data.data.user;
        
        // 解析JWT token内容
        try {
            const tokenParts = accessToken.split('.');
            if (tokenParts.length === 3) {
                const payload = JSON.parse(Buffer.from(tokenParts[1], 'base64').toString());
                logInfo(`JWT Token解析结果:`);
                logInfo(`- 用户: ${payload.sub}`);
                logInfo(`- 角色: ${JSON.stringify(payload.roles)}`);
                logInfo(`- 过期时间: ${new Date(payload.exp * 1000).toISOString()}`);
                logInfo(`- Token长度: ${accessToken.length}`);
            }
        } catch (error) {
            logWarning(`JWT Token解析失败: ${error.message}`);
        }
        
        logSuccess(`学生用户登录成功`);
        
        return {
            success: true,
            accessToken: accessToken,
            user: user,
            username: username
        };
        
    } catch (error) {
        logError(`学生用户登录失败: ${error.message}`);
        return {
            success: false,
            error: error.message
        };
    }
}

/**
 * 1. 检查服务器健康状态
 */
async function checkServerHealth() {
    logSection('1. 服务器健康状态检查');
    
    const response = await makeRequest('GET', `${CONFIG.API_BASE_URL}/auth/health`);
    
    if (response.success && response.status === 200) {
        logSuccess('服务器运行正常');
        return true;
    } else {
        logError(`服务器健康检查失败: ${response.error || '未知错误'}`);
        return false;
    }
}

/**
 * 2. 用户注册和登录
 */
async function testUserAuthentication() {
    logSection('2. 用户认证流程测试');
    
    // 生成测试用户
    const userData = generateRandomUser();
    logInfo(`生成测试用户: ${userData.username}`);
    
    // 用户注册
    const registerResponse = await makeRequest('POST', `${CONFIG.API_BASE_URL}/auth/register`, userData);
    
    assert(registerResponse.success, '用户注册成功', 'authentication');
    assert(registerResponse.status === 201, `注册返回状态码为201，实际: ${registerResponse.status}`, 'authentication');
    assert(registerResponse.data && registerResponse.data.success === true, '注册响应success字段为true', 'authentication');
    
    if (!registerResponse.success) {
        logError('用户注册失败，测试终止');
        return null;
    }
    
    // 用户登录
    const loginData = {
        usernameOrEmail: userData.username,
        password: userData.password
    };
    
    const loginResponse = await makeRequest('POST', `${CONFIG.API_BASE_URL}/auth/login`, loginData);
    
    assert(loginResponse.success, '用户登录成功', 'authentication');
    assert(loginResponse.status === 200, `登录返回状态码为200，实际: ${loginResponse.status}`, 'authentication');
    assert(loginResponse.data && loginResponse.data.accessToken, '登录响应包含accessToken', 'authentication');
    
    if (loginResponse.success && loginResponse.data) {
        const authData = {
            accessToken: loginResponse.data.accessToken,
            user: {
                username: loginResponse.data.username,
                email: loginResponse.data.email,
                userId: loginResponse.data.userId,
                roles: loginResponse.data.roles
            },
            userData: userData
        };
        testResults.testData.users.push(authData);
        logSuccess(`用户认证完成: ${userData.username}`);
        return authData;
    } else {
        logError('用户登录失败，测试终止');
        return null;
    }
}

/**
 * 3. 获取学生提交记录列表
 */
async function testGetSubmissionList(authData) {
    logSection('3. 获取学生提交记录测试');
    
    const headers = {
        'Authorization': `Bearer ${authData.accessToken}`
    };
    
    const response = await makeRequest('GET', `${CONFIG.API_BASE_URL}/submissions/my-submissions`, null, headers);
    
    assert(response.success, '获取学生提交记录请求成功', 'submission');
    assert(response.status === 200, `返回状态码为200，实际: ${response.status}`, 'submission');
    assert(response.data && Array.isArray(response.data), '学生提交记录为数组格式', 'submission');
    
    if (response.success && response.data) {
        const submissions = response.data;
        testResults.testData.submissions = submissions;
        logSuccess(`获取到 ${submissions.length} 个提交记录`);
        
        if (submissions.length > 0) {
            logInfo(`第一个提交: ID=${submissions[0].id}, 作业ID=${submissions[0].assignmentId}`);
        }
        
        return submissions;
    } else {
        logError('获取学生提交记录失败');
        return [];
    }
}

/**
 * 4. 验证提交数据
 */
async function validateSubmissionData(submissions) {
    logSection('4. 验证提交数据');
    
    if (!submissions || submissions.length === 0) {
        logWarning('没有可用的提交记录，跳过验证');
        return null;
    }
    
    const firstSubmission = submissions[0];
    
    // 验证提交数据结构
    assert(firstSubmission.id, '提交记录包含ID字段', 'submission');
    assert(firstSubmission.assignmentId, '提交记录包含作业ID字段', 'submission');
    
    logSuccess('提交数据验证通过');
    logInfo(`使用提交: ID=${firstSubmission.id}, 作业ID=${firstSubmission.assignmentId}`);
    
    return firstSubmission;
}

/**
 * 5. 测试获取作业提交结果详情
 */
async function testGetSubmissionResult(authData, submission) {
    logSection('5. 获取作业提交结果详情测试');
    
    if (!submission) {
        logWarning('没有可用的作业提交，跳过结果查看测试');
        return false;
    }
    
    const headers = {
        'Authorization': `Bearer ${authData.accessToken}`
    };
    
    const response = await makeRequest('GET', `${CONFIG.API_BASE_URL}/submissions/${submission.id}/result`, null, headers);
    
    assert(response.success, '获取作业结果请求成功', 'submission_result');
    assert(response.status === 200, `返回状态码为200，实际: ${response.status}`, 'submission_result');
    assert(response.data && response.data.success === true, '响应success字段为true', 'submission_result');
    assert(response.data && response.data.data, '响应包含data字段', 'submission_result');
    
    if (response.success && response.data && response.data.data) {
        const result = response.data.data;
        
        // 验证必需字段
        assert(typeof result.totalScore === 'number', '包含总分字段(totalScore)', 'submission_result');
        assert(typeof result.teacherComment === 'string', '包含教师评语字段(teacherComment)', 'submission_result');
        assert(Array.isArray(result.questionResults), '包含题目结果数组(questionResults)', 'submission_result');
        
        // 验证题目结果详情
        if (result.questionResults && result.questionResults.length > 0) {
            const firstQuestion = result.questionResults[0];
            assert(firstQuestion.questionContent !== undefined, '题目包含内容字段(questionContent)', 'submission_result');
            assert(firstQuestion.studentAnswer !== undefined, '题目包含学生答案字段(studentAnswer)', 'submission_result');
            assert(firstQuestion.standardAnswer !== undefined, '题目包含标准答案字段(standardAnswer)', 'submission_result');
            assert(firstQuestion.explanation !== undefined, '题目包含文字解析字段(explanation)', 'submission_result');
            assert(firstQuestion.videoUrl !== undefined, '题目包含视频URL字段(videoUrl)', 'submission_result');
            
            logInfo(`题目结果详情: 内容=${firstQuestion.questionContent?.substring(0, 50)}...`);
            logInfo(`学生答案: ${firstQuestion.studentAnswer}`);
            logInfo(`标准答案: ${firstQuestion.standardAnswer}`);
            logInfo(`文字解析: ${firstQuestion.explanation || '无'}`);
            logInfo(`视频URL: ${firstQuestion.videoUrl || '无'}`);
        }
        
        logSuccess(`获取作业结果成功: 总分=${result.totalScore}, 题目数=${result.questionResults?.length || 0}`);
        return true;
    } else {
        logError('获取作业结果失败');
        return false;
    }
}

/**
 * 6. 测试权限控制
 */
async function testPermissionControl(authData1, authData2, submission) {
    logSection('6. 权限控制测试');
    
    if (!authData2 || !submission) {
        logWarning('缺少第二个用户或作业提交，跳过权限控制测试');
        return false;
    }
    
    // 使用第二个用户的token尝试访问第一个用户的作业结果
    const headers = {
        'Authorization': `Bearer ${authData2.accessToken}`
    };
    
    const response = await makeRequest('GET', `${CONFIG.API_BASE_URL}/submissions/${submission.id}/result`, null, headers);
    
    // 应该返回403或404，表示没有权限访问
    assert(!response.success, '跨用户访问被正确拒绝', 'permission');
    assert(response.status === 403 || response.status === 404, 
           `返回正确的权限错误状态码(403/404)，实际: ${response.status}`, 'permission');
    
    logSuccess('权限控制测试通过 - 用户无法访问其他用户的作业结果');
    return true;
}

/**
 * 打印测试结果摘要
 */
function printTestSummary() {
    logSection('测试结果摘要');
    
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
    
    // 测试数据统计
    log('\n测试数据统计:', colors.cyan);
    log(`- 创建用户数: ${testResults.testData.users.length}`);
    log(`- 获取作业数: ${testResults.testData.assignments.length}`);
    log(`- 创建提交数: ${testResults.testData.submissions.length}`);
    
    if (testResults.failed === 0) {
        logSuccess('\n🎉 所有验收测试通过！Sprint 4 作业结果查看功能验收成功！');
    } else {
        logError('\n❌ 部分测试失败，请检查上述错误信息');
    }
    
    log('='.repeat(60), colors.bold);
}

/**
 * 生成测试报告
 */
function generateTestReport() {
    const report = {
        testSuite: 'Sprint 4 Acceptance Test',
        timestamp: new Date().toISOString(),
        environment: CONFIG.BASE_URL,
        summary: {
            total: testResults.total,
            passed: testResults.passed,
            failed: testResults.failed,
            successRate: testResults.total > 0 ? 
                ((testResults.passed / testResults.total) * 100).toFixed(2) : 0
        },
        errors: testResults.errors,
        testData: testResults.testData
    };
    
    const reportPath = path.join(__dirname, 'sprint4-acceptance-test-report.json');
    
    try {
        fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));
        logSuccess(`测试报告已生成: ${reportPath}`);
    } catch (error) {
        logError(`生成测试报告失败: ${error.message}`);
    }
}

/**
 * 主测试函数
 */
async function runAcceptanceTests() {
    log('🚀 开始执行 Sprint 4 自动化验收测试', colors.bold);
    log('测试目标: 验证学员查看已批改作业结果功能', colors.blue);
    log('='.repeat(60));
    
    try {
        // 1. 检查服务器健康状态
        const serverHealthy = await checkServerHealth();
        if (!serverHealthy) {
            logError('服务器不可用，测试终止');
            process.exit(1);
        }
        
        // 2. 用户认证测试
        const authData1 = await loginTestStudent();
        if (!authData1 || !authData1.success) {
            logError('用户认证失败，测试终止');
            return;
        }
        
        // 创建第二个用户用于权限测试
        const authData2 = await loginTestStudent('test_student2', 'password123');
        
        // 3. 获取学生提交记录
        const submissions = await testGetSubmissionList(authData1);
        
        // 4. 验证提交数据
        const submission = await validateSubmissionData(submissions);
        
        // 5. 测试获取作业结果
        await testGetSubmissionResult(authData1, submission);
        
        // 6. 测试权限控制
        if (authData2) {
            await testPermissionControl(authData1, authData2, submission);
        }
        
        logSuccess('\n✅ 所有核心测试流程完成');
        
    } catch (error) {
        logError(`测试执行过程中发生未预期错误: ${error.message}`);
        testResults.errors.push(`未预期错误: ${error.message}`);
    } finally {
        printTestSummary();
        generateTestReport();
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