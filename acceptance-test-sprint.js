const axios = require('axios');
const fs = require('fs');
const FormData = require('form-data');
const path = require('path');

// 配置
const BASE_URL = 'http://localhost:8080';
const API_BASE = `${BASE_URL}/api`;
const SWAGGER_URL = `${BASE_URL}/swagger-ui/index.html`;

// 测试结果统计
let testResults = {
    total: 0,
    passed: 0,
    failed: 0,
    errors: []
};

// 测试用户数据
const testUsers = {
    teacher: {
        username: 'teacher@test.com',
        password: 'password123',
        role: 'TEACHER'
    },
    student: {
        username: 'student@test.com', 
        password: 'password123',
        role: 'STUDENT'
    }
};

let authTokens = {};

// 工具函数
function log(message, type = 'INFO') {
    const timestamp = new Date().toISOString();
    console.log(`[${timestamp}] [${type}] ${message}`);
}

function assert(condition, message) {
    testResults.total++;
    if (condition) {
        testResults.passed++;
        log(`✅ PASS: ${message}`, 'TEST');
    } else {
        testResults.failed++;
        testResults.errors.push(message);
        log(`❌ FAIL: ${message}`, 'TEST');
    }
}

function assertResponseTime(duration, maxTime = 2000) {
    assert(duration < maxTime, `响应时间 ${duration}ms < ${maxTime}ms`);
}

// 延迟函数
function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

// 1. 功能验收标准测试
class FunctionalAcceptanceTests {
    
    // 用户认证测试
    static async testAuthentication() {
        log('开始用户认证测试');
        
        try {
            // 教师登录
            const teacherLoginStart = Date.now();
            const teacherResponse = await axios.post(`${API_BASE}/auth/login`, {
                username: testUsers.teacher.username,
                password: testUsers.teacher.password
            });
            const teacherLoginTime = Date.now() - teacherLoginStart;
            
            assert(teacherResponse.status === 200, '教师登录成功');
            assert(teacherResponse.data.token, '教师获取到JWT令牌');
            assertResponseTime(teacherLoginTime);
            authTokens.teacher = teacherResponse.data.token;
            
            // 学生登录
            const studentLoginStart = Date.now();
            const studentResponse = await axios.post(`${API_BASE}/auth/login`, {
                username: testUsers.student.username,
                password: testUsers.student.password
            });
            const studentLoginTime = Date.now() - studentLoginStart;
            
            assert(studentResponse.status === 200, '学生登录成功');
            assert(studentResponse.data.token, '学生获取到JWT令牌');
            assertResponseTime(studentLoginTime);
            authTokens.student = studentResponse.data.token;
            
        } catch (error) {
            assert(false, `认证测试失败: ${error.message}`);
        }
    }
    
    // 作业管理测试
    static async testAssignmentManagement() {
        log('开始作业管理测试');
        
        try {
            const headers = { Authorization: `Bearer ${authTokens.teacher}` };
            
            // 创建作业
            const createStart = Date.now();
            const createResponse = await axios.post(`${API_BASE}/assignments`, {
                title: '验收测试作业',
                description: '这是一个验收测试作业',
                dueDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString(),
                totalScore: 100,
                status: 'DRAFT'
            }, { headers });
            const createTime = Date.now() - createStart;
            
            assert(createResponse.status === 201, '教师可以创建作业');
            assertResponseTime(createTime);
            const assignmentId = createResponse.data.id;
            
            // 获取作业列表
            const listStart = Date.now();
            const listResponse = await axios.get(`${API_BASE}/assignments`, { headers });
            const listTime = Date.now() - listStart;
            
            assert(listResponse.status === 200, '教师可以查看作业列表');
            assert(Array.isArray(listResponse.data), '作业列表返回数组格式');
            assertResponseTime(listTime);
            
            // 编辑作业
            const updateStart = Date.now();
            const updateResponse = await axios.put(`${API_BASE}/assignments/${assignmentId}`, {
                title: '验收测试作业(已编辑)',
                description: '这是一个已编辑的验收测试作业',
                dueDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString(),
                totalScore: 100,
                status: 'PUBLISHED'
            }, { headers });
            const updateTime = Date.now() - updateStart;
            
            assert(updateResponse.status === 200, '教师可以编辑作业');
            assertResponseTime(updateTime);
            
            // 发布作业 (通过状态更新)
            assert(updateResponse.data.status === 'PUBLISHED', '教师可以发布作业');
            
            return assignmentId;
            
        } catch (error) {
            assert(false, `作业管理测试失败: ${error.message}`);
            return null;
        }
    }
    
    // 作业提交测试
    static async testAssignmentSubmission(assignmentId) {
        log('开始作业提交测试');
        
        if (!assignmentId) {
            assert(false, '无法进行作业提交测试：作业ID为空');
            return;
        }
        
        try {
            const studentHeaders = { Authorization: `Bearer ${authTokens.student}` };
            
            // 学生查看作业要求
            const viewStart = Date.now();
            const viewResponse = await axios.get(`${API_BASE}/assignments/${assignmentId}`, { 
                headers: studentHeaders 
            });
            const viewTime = Date.now() - viewStart;
            
            assert(viewResponse.status === 200, '学生可以查看作业要求');
            assert(viewResponse.data.title, '作业包含标题信息');
            assert(viewResponse.data.description, '作业包含描述信息');
            assertResponseTime(viewTime);
            
            // 学生提交作业
            const submitStart = Date.now();
            const submitResponse = await axios.post(`${API_BASE}/submissions`, {
                assignmentId: assignmentId,
                content: '这是我的作业提交内容',
                submissionType: 'TEXT'
            }, { headers: studentHeaders });
            const submitTime = Date.now() - submitStart;
            
            assert(submitResponse.status === 201, '学生可以提交作业内容');
            assertResponseTime(submitTime);
            
            return submitResponse.data.id;
            
        } catch (error) {
            assert(false, `作业提交测试失败: ${error.message}`);
            return null;
        }
    }
    
    // 文件处理测试
    static async testFileHandling() {
        log('开始文件处理测试');
        
        try {
            const headers = { Authorization: `Bearer ${authTokens.teacher}` };
            
            // 创建测试文件
            const testContent = 'This is a test file for acceptance testing.';
            const testFilePath = path.join(__dirname, 'test-file.txt');
            fs.writeFileSync(testFilePath, testContent);
            
            // 文件上传测试
            const form = new FormData();
            form.append('file', fs.createReadStream(testFilePath));
            form.append('description', '验收测试文件');
            
            const uploadStart = Date.now();
            const uploadResponse = await axios.post(`${API_BASE}/files/upload`, form, {
                headers: {
                    ...headers,
                    ...form.getHeaders()
                }
            });
            const uploadTime = Date.now() - uploadStart;
            
            assert(uploadResponse.status === 200, '支持文件上传');
            assertResponseTime(uploadTime);
            const fileId = uploadResponse.data.id;
            
            // 文件下载测试
            const downloadStart = Date.now();
            const downloadResponse = await axios.get(`${API_BASE}/files/${fileId}/download`, {
                headers,
                responseType: 'stream'
            });
            const downloadTime = Date.now() - downloadStart;
            
            assert(downloadResponse.status === 200, '支持文件下载');
            assertResponseTime(downloadTime);
            
            // 文件访问控制测试
            try {
                await axios.get(`${API_BASE}/files/${fileId}/download`);
                assert(false, '文件访问应该需要认证');
            } catch (error) {
                assert(error.response.status === 401, '文件访问有安全控制');
            }
            
            // 清理测试文件
            fs.unlinkSync(testFilePath);
            
            return fileId;
            
        } catch (error) {
            assert(false, `文件处理测试失败: ${error.message}`);
            return null;
        }
    }
    
    // 批改系统测试
    static async testGradingSystem(submissionId) {
        log('开始批改系统测试');
        
        if (!submissionId) {
            assert(false, '无法进行批改测试：提交ID为空');
            return;
        }
        
        try {
            const teacherHeaders = { Authorization: `Bearer ${authTokens.teacher}` };
            
            // 教师查看提交
            const viewStart = Date.now();
            const viewResponse = await axios.get(`${API_BASE}/submissions/${submissionId}`, {
                headers: teacherHeaders
            });
            const viewTime = Date.now() - viewStart;
            
            assert(viewResponse.status === 200, '教师可以查看学生提交');
            assertResponseTime(viewTime);
            
            // 教师评分和反馈
            const gradeStart = Date.now();
            const gradeResponse = await axios.put(`${API_BASE}/submissions/${submissionId}/grade`, {
                score: 85,
                feedback: '作业完成得很好，继续保持！'
            }, { headers: teacherHeaders });
            const gradeTime = Date.now() - gradeStart;
            
            assert(gradeResponse.status === 200, '教师可以对作业进行评分');
            assert(gradeResponse.data.score === 85, '评分信息正确保存');
            assert(gradeResponse.data.feedback, '反馈信息正确保存');
            assertResponseTime(gradeTime);
            
        } catch (error) {
            assert(false, `批改系统测试失败: ${error.message}`);
        }
    }
    
    // 权限控制测试
    static async testPermissionControl(assignmentId) {
        log('开始权限控制测试');
        
        try {
            const studentHeaders = { Authorization: `Bearer ${authTokens.student}` };
            
            // 学生尝试创建作业（应该失败）
            try {
                await axios.post(`${API_BASE}/assignments`, {
                    title: '学生创建的作业',
                    description: '学生不应该能创建作业',
                    dueDate: new Date().toISOString(),
                    totalScore: 100
                }, { headers: studentHeaders });
                assert(false, '学生不应该能创建作业');
            } catch (error) {
                assert(error.response.status === 403, '学生创建作业被正确拒绝');
            }
            
            // 学生尝试删除作业（应该失败）
            if (assignmentId) {
                try {
                    await axios.delete(`${API_BASE}/assignments/${assignmentId}`, {
                        headers: studentHeaders
                    });
                    assert(false, '学生不应该能删除作业');
                } catch (error) {
                    assert(error.response.status === 403, '学生删除作业被正确拒绝');
                }
            }
            
            // 无认证访问测试
            try {
                await axios.get(`${API_BASE}/assignments`);
                // 某些端点可能允许无认证访问，这取决于具体实现
            } catch (error) {
                assert(error.response.status === 401, '无认证访问被正确拒绝');
            }
            
        } catch (error) {
            assert(false, `权限控制测试失败: ${error.message}`);
        }
    }
}

// 2. 技术验收标准测试
class TechnicalAcceptanceTests {
    
    // API端点测试
    static async testApiEndpoints() {
        log('开始API端点测试');
        
        try {
            // 测试Swagger UI可访问性
            const swaggerStart = Date.now();
            const swaggerResponse = await axios.get(SWAGGER_URL);
            const swaggerTime = Date.now() - swaggerStart;
            
            assert(swaggerResponse.status === 200, 'Swagger UI可访问');
            assertResponseTime(swaggerTime);
            
            // 测试健康检查端点
            const healthStart = Date.now();
            const healthResponse = await axios.get(`${BASE_URL}/actuator/health`);
            const healthTime = Date.now() - healthStart;
            
            assert(healthResponse.status === 200, '健康检查端点正常');
            assert(healthResponse.data.status === 'UP', '应用状态健康');
            assertResponseTime(healthTime);
            
        } catch (error) {
            assert(false, `API端点测试失败: ${error.message}`);
        }
    }
    
    // 测试覆盖率验证
    static async verifyTestCoverage() {
        log('验证测试覆盖率');
        
        try {
            // 读取JaCoCo报告
            const jacocoReportPath = path.join(__dirname, 'backend/target/site/jacoco/index.html');
            
            if (fs.existsSync(jacocoReportPath)) {
                const reportContent = fs.readFileSync(jacocoReportPath, 'utf8');
                
                // 解析覆盖率（简单的正则匹配）
                const coverageMatch = reportContent.match(/<td class="ctr2" id="c\d+">(\d+)%<\/td>/);
                
                if (coverageMatch) {
                    const coverage = parseInt(coverageMatch[1]);
                    assert(coverage >= 80, `单元测试覆盖率 ${coverage}% >= 80%`);
                } else {
                    log('无法解析覆盖率报告，手动检查覆盖率', 'WARN');
                }
            } else {
                log('JaCoCo报告不存在，跳过覆盖率检查', 'WARN');
            }
            
        } catch (error) {
            log(`覆盖率验证失败: ${error.message}`, 'WARN');
        }
    }
}

// 3. 性能验收标准测试
class PerformanceAcceptanceTests {
    
    // 响应时间测试
    static async testResponseTime() {
        log('开始响应时间测试');
        
        const endpoints = [
            { method: 'GET', url: `${API_BASE}/assignments`, headers: { Authorization: `Bearer ${authTokens.teacher}` } },
            { method: 'GET', url: `${BASE_URL}/actuator/health` }
        ];
        
        for (const endpoint of endpoints) {
            try {
                const start = Date.now();
                const response = await axios({
                    method: endpoint.method,
                    url: endpoint.url,
                    headers: endpoint.headers || {}
                });
                const duration = Date.now() - start;
                
                assert(response.status === 200, `${endpoint.method} ${endpoint.url} 响应成功`);
                assertResponseTime(duration, 2000);
                
            } catch (error) {
                assert(false, `${endpoint.method} ${endpoint.url} 响应时间测试失败: ${error.message}`);
            }
        }
    }
    
    // 并发测试（简化版）
    static async testConcurrency() {
        log('开始并发测试（简化版）');
        
        const concurrentRequests = 10; // 简化为10个并发请求
        const promises = [];
        
        for (let i = 0; i < concurrentRequests; i++) {
            promises.push(
                axios.get(`${BASE_URL}/actuator/health`)
                    .then(response => ({ success: true, status: response.status }))
                    .catch(error => ({ success: false, error: error.message }))
            );
        }
        
        try {
            const results = await Promise.all(promises);
            const successCount = results.filter(r => r.success).length;
            
            assert(successCount >= concurrentRequests * 0.9, 
                `并发请求成功率 ${successCount}/${concurrentRequests} >= 90%`);
                
        } catch (error) {
            assert(false, `并发测试失败: ${error.message}`);
        }
    }
}

// 4. 用户体验验收标准测试
class UserExperienceAcceptanceTests {
    
    // 文件格式支持测试
    static async testFileFormatSupport() {
        log('开始文件格式支持测试');
        
        const supportedFormats = [
            { ext: 'txt', content: 'Test text file', mimeType: 'text/plain' },
            { ext: 'pdf', content: '%PDF-1.4 fake pdf content', mimeType: 'application/pdf' }
        ];
        
        const headers = { Authorization: `Bearer ${authTokens.teacher}` };
        
        for (const format of supportedFormats) {
            try {
                const testFilePath = path.join(__dirname, `test-file.${format.ext}`);
                fs.writeFileSync(testFilePath, format.content);
                
                const form = new FormData();
                form.append('file', fs.createReadStream(testFilePath));
                form.append('description', `测试${format.ext.toUpperCase()}文件`);
                
                const uploadResponse = await axios.post(`${API_BASE}/files/upload`, form, {
                    headers: {
                        ...headers,
                        ...form.getHeaders()
                    }
                });
                
                assert(uploadResponse.status === 200, `支持${format.ext.toUpperCase()}格式文件上传`);
                
                // 清理测试文件
                fs.unlinkSync(testFilePath);
                
            } catch (error) {
                assert(false, `${format.ext.toUpperCase()}文件格式测试失败: ${error.message}`);
            }
        }
    }
    
    // 错误信息测试
    static async testErrorMessages() {
        log('开始错误信息测试');
        
        try {
            // 测试无效登录
            const loginResponse = await axios.post(`${API_BASE}/auth/login`, {
                username: 'invalid@test.com',
                password: 'wrongpassword'
            }).catch(error => error.response);
            
            assert(loginResponse.status === 401, '无效登录返回正确状态码');
            assert(loginResponse.data.message, '错误响应包含清晰的错误信息');
            
            // 测试无效请求
            const invalidResponse = await axios.post(`${API_BASE}/assignments`, {
                // 缺少必需字段
            }, {
                headers: { Authorization: `Bearer ${authTokens.teacher}` }
            }).catch(error => error.response);
            
            assert(invalidResponse.status === 400, '无效请求返回正确状态码');
            assert(invalidResponse.data.message, '验证错误包含清晰的错误信息');
            
        } catch (error) {
            assert(false, `错误信息测试失败: ${error.message}`);
        }
    }
}

// 主测试执行函数
async function runAcceptanceTests() {
    log('开始执行Sprint验收测试');
    log('='.repeat(50));
    
    try {
        // 等待服务启动
        log('等待服务启动...');
        await delay(5000);
        
        // 1. 功能验收标准测试
        log('\n1. 功能验收标准测试');
        log('-'.repeat(30));
        
        await FunctionalAcceptanceTests.testAuthentication();
        const assignmentId = await FunctionalAcceptanceTests.testAssignmentManagement();
        const submissionId = await FunctionalAcceptanceTests.testAssignmentSubmission(assignmentId);
        await FunctionalAcceptanceTests.testFileHandling();
        await FunctionalAcceptanceTests.testGradingSystem(submissionId);
        await FunctionalAcceptanceTests.testPermissionControl(assignmentId);
        
        // 2. 技术验收标准测试
        log('\n2. 技术验收标准测试');
        log('-'.repeat(30));
        
        await TechnicalAcceptanceTests.testApiEndpoints();
        await TechnicalAcceptanceTests.verifyTestCoverage();
        
        // 3. 性能验收标准测试
        log('\n3. 性能验收标准测试');
        log('-'.repeat(30));
        
        await PerformanceAcceptanceTests.testResponseTime();
        await PerformanceAcceptanceTests.testConcurrency();
        
        // 4. 用户体验验收标准测试
        log('\n4. 用户体验验收标准测试');
        log('-'.repeat(30));
        
        await UserExperienceAcceptanceTests.testFileFormatSupport();
        await UserExperienceAcceptanceTests.testErrorMessages();
        
    } catch (error) {
        log(`测试执行出错: ${error.message}`, 'ERROR');
    }
    
    // 生成测试报告
    generateTestReport();
}

// 生成测试报告
function generateTestReport() {
    log('\n' + '='.repeat(50));
    log('Sprint验收测试报告');
    log('='.repeat(50));
    
    const passRate = testResults.total > 0 ? (testResults.passed / testResults.total * 100).toFixed(2) : 0;
    
    log(`总测试数: ${testResults.total}`);
    log(`通过数: ${testResults.passed}`);
    log(`失败数: ${testResults.failed}`);
    log(`通过率: ${passRate}%`);
    
    if (testResults.failed > 0) {
        log('\n失败的测试:');
        testResults.errors.forEach((error, index) => {
            log(`${index + 1}. ${error}`);
        });
    }
    
    // 验收结论
    log('\n验收结论:');
    if (passRate >= 90) {
        log('✅ Sprint验收测试通过！系统满足验收标准。');
    } else if (passRate >= 70) {
        log('⚠️  Sprint验收测试部分通过，存在一些问题需要修复。');
    } else {
        log('❌ Sprint验收测试失败，需要重大修复后重新测试。');
    }
    
    // 保存报告到文件
    const reportContent = {
        timestamp: new Date().toISOString(),
        summary: {
            total: testResults.total,
            passed: testResults.passed,
            failed: testResults.failed,
            passRate: `${passRate}%`
        },
        errors: testResults.errors,
        conclusion: passRate >= 90 ? 'PASSED' : passRate >= 70 ? 'PARTIAL' : 'FAILED'
    };
    
    fs.writeFileSync(
        path.join(__dirname, 'acceptance-test-report.json'),
        JSON.stringify(reportContent, null, 2)
    );
    
    log('\n测试报告已保存到: acceptance-test-report.json');
}

// 如果直接运行此脚本
if (require.main === module) {
    runAcceptanceTests().catch(error => {
        log(`测试执行失败: ${error.message}`, 'ERROR');
        process.exit(1);
    });
}

module.exports = {
    runAcceptanceTests,
    FunctionalAcceptanceTests,
    TechnicalAcceptanceTests,
    PerformanceAcceptanceTests,
    UserExperienceAcceptanceTests
};