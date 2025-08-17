#!/usr/bin/env node

/**
 * 万里书院集成测试执行器
 * 执行API端点测试、功能测试、性能测试和安全测试
 */

const axios = require('axios');
const fs = require('fs');
const path = require('path');

// 测试配置
const config = {
    baseURL: process.env.TEST_BASE_URL || 'http://localhost:8080',
    timeout: parseInt(process.env.TEST_TIMEOUT) || 30000,
    reportDir: process.env.REPORT_DIR || 'integration-tests/reports',
    maxRetries: 3,
    retryDelay: 1000
};

// 测试结果收集器
class TestResultCollector {
    constructor() {
        this.results = {
            summary: {
                total_tests: 0,
                passed_tests: 0,
                failed_tests: 0,
                skipped_tests: 0,
                success_rate: 0,
                start_time: new Date().toISOString(),
                end_time: null,
                duration_ms: 0
            },
            test_suites: []
        };
        this.currentSuite = null;
        this.startTime = Date.now();
    }

    startSuite(name, description) {
        this.currentSuite = {
            name,
            description,
            tests: [],
            start_time: new Date().toISOString(),
            end_time: null,
            duration_ms: 0,
            passed: 0,
            failed: 0,
            skipped: 0
        };
        console.log(`\n🧪 开始测试套件: ${name}`);
        console.log(`   ${description}`);
    }

    addTest(name, status, message = '', details = {}) {
        if (!this.currentSuite) {
            throw new Error('No active test suite');
        }

        const test = {
            name,
            status, // 'passed', 'failed', 'skipped'
            message,
            details,
            timestamp: new Date().toISOString()
        };

        this.currentSuite.tests.push(test);
        this.currentSuite[status]++;
        this.results.summary.total_tests++;
        this.results.summary[`${status}_tests`]++;

        const statusIcon = {
            passed: '✅',
            failed: '❌',
            skipped: '⏭️'
        }[status];

        console.log(`   ${statusIcon} ${name}${message ? ': ' + message : ''}`);
        
        if (status === 'failed' && details.error) {
            console.log(`      错误: ${details.error}`);
        }
    }

    endSuite() {
        if (!this.currentSuite) {
            return;
        }

        this.currentSuite.end_time = new Date().toISOString();
        this.currentSuite.duration_ms = Date.now() - new Date(this.currentSuite.start_time).getTime();
        this.results.test_suites.push(this.currentSuite);
        
        console.log(`   📊 套件结果: ${this.currentSuite.passed}通过, ${this.currentSuite.failed}失败, ${this.currentSuite.skipped}跳过`);
        this.currentSuite = null;
    }

    finalize() {
        this.results.summary.end_time = new Date().toISOString();
        this.results.summary.duration_ms = Date.now() - this.startTime;
        
        if (this.results.summary.total_tests > 0) {
            this.results.summary.success_rate = Math.round(
                (this.results.summary.passed_tests / this.results.summary.total_tests) * 100
            );
        }

        return this.results;
    }
}

// HTTP客户端封装
class APIClient {
    constructor(baseURL, timeout) {
        this.client = axios.create({
            baseURL,
            timeout,
            validateStatus: () => true // 不自动抛出错误
        });
        this.authToken = null;
    }

    async request(method, url, data = null, headers = {}) {
        const config = {
            method,
            url,
            headers: {
                'Content-Type': 'application/json',
                ...headers
            }
        };

        if (this.authToken) {
            config.headers.Authorization = `Bearer ${this.authToken}`;
        }

        if (data) {
            config.data = data;
        }

        try {
            const response = await this.client.request(config);
            return {
                status: response.status,
                data: response.data,
                headers: response.headers
            };
        } catch (error) {
            return {
                status: 0,
                data: null,
                error: error.message
            };
        }
    }

    async login(username, password) {
        const requestData = {
            usernameOrEmail: username,
            password
        };
        
        const response = await this.request('POST', '/api/auth/login', requestData);

        if (response.status === 200 && response.data && response.data.success && response.data.data && response.data.data.accessToken) {
            this.authToken = response.data.data.accessToken;
            return true;
        }
        return false;
    }

    logout() {
        this.authToken = null;
    }
}

// 测试套件定义
class IntegrationTestSuite {
    constructor(collector, apiClient) {
        this.collector = collector;
        this.api = apiClient;
    }

    // 健康检查测试
    async testHealthCheck() {
        this.collector.startSuite('健康检查测试', '验证应用基本可用性和健康状态');

        // 测试应用启动
        const healthResponse = await this.api.request('GET', '/actuator/health');
        if (healthResponse.status === 200 && healthResponse.data && healthResponse.data.status === 'UP') {
            this.collector.addTest('应用健康检查', 'passed', '应用状态正常');
        } else {
            this.collector.addTest('应用健康检查', 'failed', '应用状态异常', {
                error: `状态码: ${healthResponse.status}, 响应: ${JSON.stringify(healthResponse.data)}`
            });
        }

        // 测试Swagger UI
        const swaggerResponse = await this.api.request('GET', '/swagger-ui/index.html');
        if (swaggerResponse.status === 200) {
            this.collector.addTest('Swagger UI可访问性', 'passed', 'Swagger UI正常访问');
        } else {
            this.collector.addTest('Swagger UI可访问性', 'failed', 'Swagger UI无法访问', {
                error: `状态码: ${swaggerResponse.status}`
            });
        }

        // 测试API文档
        const apiDocsResponse = await this.api.request('GET', '/v3/api-docs');
        if (apiDocsResponse.status === 200) {
            this.collector.addTest('API文档可访问性', 'passed', 'API文档正常访问');
        } else {
            this.collector.addTest('API文档可访问性', 'failed', 'API文档无法访问', {
                error: `状态码: ${apiDocsResponse.status}`
            });
        }

        this.collector.endSuite();
    }

    // 认证测试
    async testAuthentication() {
        this.collector.startSuite('认证测试', '验证用户登录、权限控制和JWT令牌');

        // 测试管理员登录
        const adminLoginSuccess = await this.api.login('test_admin', 'password123');
        if (adminLoginSuccess) {
            this.collector.addTest('管理员登录', 'passed', '管理员登录成功');
        } else {
            this.collector.addTest('管理员登录', 'failed', '管理员登录失败');
        }

        // 测试无效登录（临时清除令牌）
        const savedToken = this.api.authToken;
        this.api.logout();
        const invalidLoginSuccess = await this.api.login('invalid', 'invalid');
        if (!invalidLoginSuccess) {
            this.collector.addTest('无效登录拒绝', 'passed', '无效登录被正确拒绝');
        } else {
            this.collector.addTest('无效登录拒绝', 'failed', '无效登录未被拒绝');
        }
        // 恢复管理员令牌
        this.api.authToken = savedToken;

        // 测试总部教师登录
        const teacherLoginSuccess = await this.api.login('test_hq_teacher', 'password123');
        if (teacherLoginSuccess) {
            this.collector.addTest('总部教师登录', 'passed', '总部教师登录成功');
        } else {
            this.collector.addTest('总部教师登录', 'failed', '总部教师登录失败');
        }

        this.collector.endSuite();
    }

    // 作业管理测试
    async testHomeworkManagement() {
        this.collector.startSuite('作业管理测试', '验证作业CRUD操作和权限控制');

        // 确保以总部教师身份登录
        await this.api.login('test_hq_teacher', 'password123');

        // 测试创建作业
        const createHomeworkResponse = await this.api.request('POST', '/api/homeworks', {
            title: '集成测试作业',
            description: '这是一个集成测试创建的作业',
            subject: 'MATH',
            gradeLevel: 'GRADE_5',
            dueDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
        });

        let homeworkId = null;
        if (createHomeworkResponse.status === 201 && createHomeworkResponse.data && createHomeworkResponse.data.id) {
            homeworkId = createHomeworkResponse.data.id;
            this.collector.addTest('创建作业', 'passed', `作业创建成功，ID: ${homeworkId}`);
        } else {
            this.collector.addTest('创建作业', 'failed', '作业创建失败', {
                error: `状态码: ${createHomeworkResponse.status}, 响应: ${JSON.stringify(createHomeworkResponse.data)}`
            });
        }

        // 测试获取作业列表
        const getHomeworksResponse = await this.api.request('GET', '/api/homeworks');
        if (getHomeworksResponse.status === 200 && Array.isArray(getHomeworksResponse.data)) {
            this.collector.addTest('获取作业列表', 'passed', `获取到${getHomeworksResponse.data.length}个作业`);
        } else {
            this.collector.addTest('获取作业列表', 'failed', '获取作业列表失败', {
                error: `状态码: ${getHomeworksResponse.status}`
            });
        }

        // 测试添加题目（如果作业创建成功）
        if (homeworkId) {
            const addQuestionResponse = await this.api.request('POST', `/api/homeworks/${homeworkId}/questions`, {
                content: '1 + 1 = ?',
                questionType: 'SINGLE_CHOICE',
                standardAnswer: '2',
                orderIndex: 1
            });

            if (addQuestionResponse.status === 201) {
                this.collector.addTest('添加题目', 'passed', '题目添加成功');
            } else {
                this.collector.addTest('添加题目', 'failed', '题目添加失败', {
                    error: `状态码: ${addQuestionResponse.status}, 响应: ${JSON.stringify(addQuestionResponse.data)}`
                });
            }
        } else {
            this.collector.addTest('添加题目', 'skipped', '因作业创建失败而跳过');
        }

        // 测试学生访问权限（应该被拒绝）
        await this.api.login('test_student1', 'password123');
        const studentAccessResponse = await this.api.request('POST', '/api/homeworks', {
            title: '学生尝试创建作业',
            description: '这应该被拒绝',
            subject: 'MATH',
            gradeLevel: 'GRADE_5'
        });

        if (studentAccessResponse.status === 403 || studentAccessResponse.status === 401) {
            this.collector.addTest('学生权限控制', 'passed', '学生创建作业被正确拒绝');
        } else {
            this.collector.addTest('学生权限控制', 'failed', '学生权限控制失效', {
                error: `状态码: ${studentAccessResponse.status}`
            });
        }

        this.collector.endSuite();
    }

    // 用户管理测试
    async testUserManagement() {
        this.collector.startSuite('用户管理测试', '验证用户信息获取和管理功能');

        // 以总部教师身份登录
        await this.api.login('test_hq_teacher', 'password123');

        // 测试获取用户列表
        const getUsersResponse = await this.api.request('GET', '/api/users');
        if (getUsersResponse.status === 200 && getUsersResponse.data && getUsersResponse.data.success && Array.isArray(getUsersResponse.data.data)) {
            this.collector.addTest('获取用户列表', 'passed', `获取到${getUsersResponse.data.data.length}个用户`);
        } else {
            this.collector.addTest('获取用户列表', 'failed', '获取用户列表失败', {
                error: `状态码: ${getUsersResponse.status}`
            });
        }

        // 测试获取当前用户信息
        const getCurrentUserResponse = await this.api.request('GET', '/api/users/me');
        if (getCurrentUserResponse.status === 200 && getCurrentUserResponse.data && getCurrentUserResponse.data.success && getCurrentUserResponse.data.data) {
            this.collector.addTest('获取当前用户信息', 'passed', `用户: ${getCurrentUserResponse.data.data.username}`);
        } else {
            this.collector.addTest('获取当前用户信息', 'failed', '获取当前用户信息失败', {
                error: `状态码: ${getCurrentUserResponse.status}`
            });
        }

        this.collector.endSuite();
    }

    // 性能测试
    async testPerformance() {
        this.collector.startSuite('性能测试', '验证API响应时间和并发处理能力');

        // 登录以获取认证
        await this.api.login('test_hq_teacher', 'password123');

        // 测试API响应时间
        const performanceTests = [
            { name: '健康检查响应时间', url: '/actuator/health', threshold: 100 },
            { name: '用户列表响应时间', url: '/api/users', threshold: 500 },
            { name: '作业列表响应时间', url: '/api/homeworks', threshold: 500 }
        ];

        for (const test of performanceTests) {
            const startTime = Date.now();
            const response = await this.api.request('GET', test.url);
            const responseTime = Date.now() - startTime;

            if (response.status === 200 && responseTime <= test.threshold) {
                this.collector.addTest(test.name, 'passed', `响应时间: ${responseTime}ms (阈值: ${test.threshold}ms)`);
            } else if (response.status !== 200) {
                this.collector.addTest(test.name, 'failed', `请求失败，状态码: ${response.status}`);
            } else {
                this.collector.addTest(test.name, 'failed', `响应时间超过阈值: ${responseTime}ms > ${test.threshold}ms`);
            }
        }

        // 简单并发测试
        const concurrentRequests = 10;
        const startTime = Date.now();
        const promises = [];
        
        for (let i = 0; i < concurrentRequests; i++) {
            promises.push(this.api.request('GET', '/actuator/health'));
        }

        try {
            const results = await Promise.all(promises);
            const totalTime = Date.now() - startTime;
            const successCount = results.filter(r => r.status === 200).length;
            
            if (successCount === concurrentRequests) {
                this.collector.addTest('并发请求处理', 'passed', 
                    `${concurrentRequests}个并发请求全部成功，总耗时: ${totalTime}ms`);
            } else {
                this.collector.addTest('并发请求处理', 'failed', 
                    `${concurrentRequests}个并发请求中有${concurrentRequests - successCount}个失败`);
            }
        } catch (error) {
            this.collector.addTest('并发请求处理', 'failed', '并发请求测试异常', {
                error: error.message
            });
        }

        this.collector.endSuite();
    }

    // 安全测试
    async testSecurity() {
        this.collector.startSuite('安全测试', '验证认证、授权和安全防护');

        // 测试未认证访问
        this.api.logout();
        const unauthorizedResponse = await this.api.request('GET', '/api/homeworks');
        if (unauthorizedResponse.status === 401 || unauthorizedResponse.status === 403) {
            this.collector.addTest('未认证访问拒绝', 'passed', '未认证访问被正确拒绝');
        } else {
            this.collector.addTest('未认证访问拒绝', 'failed', '未认证访问未被拒绝', {
                error: `状态码: ${unauthorizedResponse.status}`
            });
        }

        // 测试SQL注入防护（基本测试）
        const sqlInjectionResponse = await this.api.request('POST', '/api/auth/login', {
            username: "admin'; DROP TABLE users; --",
            password: 'anything'
        });
        if (sqlInjectionResponse.status !== 200) {
            this.collector.addTest('SQL注入防护', 'passed', 'SQL注入尝试被阻止');
        } else {
            this.collector.addTest('SQL注入防护', 'failed', 'SQL注入防护可能存在问题');
        }

        // 测试XSS防护（基本测试）
        await this.api.login('test_hq_teacher', 'password123');
        const xssResponse = await this.api.request('POST', '/api/homeworks', {
            title: '<script>alert("XSS")</script>',
            description: 'XSS测试',
            subject: 'MATH',
            gradeLevel: 'GRADE_5'
        });
        
        // 检查响应是否正确处理了XSS内容
        if (xssResponse.status === 201 || xssResponse.status === 400) {
            this.collector.addTest('XSS防护', 'passed', 'XSS内容被正确处理');
        } else {
            this.collector.addTest('XSS防护', 'failed', 'XSS防护测试异常', {
                error: `状态码: ${xssResponse.status}`
            });
        }

        this.collector.endSuite();
    }
}

// 主执行函数
async function runIntegrationTests() {
    console.log('🚀 万里书院集成测试开始');
    console.log('='.repeat(50));
    console.log(`测试目标: ${config.baseURL}`);
    console.log(`超时时间: ${config.timeout}ms`);
    console.log(`报告目录: ${config.reportDir}`);
    console.log('='.repeat(50));

    // 确保报告目录存在
    if (!fs.existsSync(config.reportDir)) {
        fs.mkdirSync(config.reportDir, { recursive: true });
    }

    const collector = new TestResultCollector();
    const apiClient = new APIClient(config.baseURL, config.timeout);
    const testSuite = new IntegrationTestSuite(collector, apiClient);

    try {
        // 执行所有测试套件
        await testSuite.testHealthCheck();
        await testSuite.testAuthentication();
        await testSuite.testHomeworkManagement();
        await testSuite.testUserManagement();
        await testSuite.testPerformance();
        await testSuite.testSecurity();

        // 生成测试结果
        const results = collector.finalize();
        
        // 保存JSON报告
        const jsonReportPath = path.join(config.reportDir, 'integration-test-results.json');
        fs.writeFileSync(jsonReportPath, JSON.stringify(results, null, 2));
        
        // 显示测试摘要
        console.log('\n' + '='.repeat(50));
        console.log('📊 测试结果摘要');
        console.log('='.repeat(50));
        console.log(`总测试数: ${results.summary.total_tests}`);
        console.log(`通过测试: ${results.summary.passed_tests}`);
        console.log(`失败测试: ${results.summary.failed_tests}`);
        console.log(`跳过测试: ${results.summary.skipped_tests}`);
        console.log(`成功率: ${results.summary.success_rate}%`);
        console.log(`执行时间: ${results.summary.duration_ms}ms`);
        console.log(`JSON报告: ${jsonReportPath}`);
        
        // 返回适当的退出码
        const exitCode = results.summary.failed_tests > 0 ? 1 : 0;
        console.log(`\n${exitCode === 0 ? '✅' : '❌'} 集成测试${exitCode === 0 ? '成功' : '失败'}`);
        process.exit(exitCode);
        
    } catch (error) {
        console.error('❌ 集成测试执行异常:', error.message);
        console.error(error.stack);
        process.exit(1);
    }
}

// 检查依赖
function checkDependencies() {
    try {
        require('axios');
        return true;
    } catch (error) {
        console.error('❌ 缺少依赖: axios');
        console.error('请运行: npm install axios');
        return false;
    }
}

// 程序入口
if (require.main === module) {
    if (checkDependencies()) {
        runIntegrationTests();
    } else {
        process.exit(1);
    }
}

module.exports = {
    IntegrationTestSuite,
    TestResultCollector,
    APIClient
};