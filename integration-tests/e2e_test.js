// 完整的端到端测试脚本
// 使用Node.js 18+内置的fetch API

const BASE_URL = 'http://localhost:8080';
const FRONTEND_URL = 'http://localhost:5173';

// 测试用户凭据
const TEST_USERS = {
  student: {
    username: 'test_student1',
    password: 'password123'
  },
  teacher: {
    username: 'test_hq_teacher',
    password: 'password123'
  }
};

// 测试结果记录
const testResults = {
  passed: 0,
  failed: 0,
  errors: []
};

// 辅助函数
function log(message, type = 'INFO') {
  const timestamp = new Date().toISOString();
  console.log(`[${timestamp}] [${type}] ${message}`);
}

function assert(condition, message) {
  if (condition) {
    testResults.passed++;
    log(`✅ PASS: ${message}`, 'PASS');
  } else {
    testResults.failed++;
    testResults.errors.push(message);
    log(`❌ FAIL: ${message}`, 'FAIL');
  }
}

// API测试函数
async function testAPI(endpoint, options = {}, expectedStatus = 200) {
  try {
    const response = await fetch(`${BASE_URL}${endpoint}`, {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers
      },
      ...options
    });
    
    const data = await response.json();
    
    assert(
      response.status === expectedStatus,
      `API ${endpoint} returned status ${response.status}, expected ${expectedStatus}`
    );
    
    return { response, data };
  } catch (error) {
    testResults.failed++;
    testResults.errors.push(`API ${endpoint} failed: ${error.message}`);
    log(`❌ API ${endpoint} failed: ${error.message}`, 'ERROR');
    return null;
  }
}

// 1. 测试后端服务健康检查
async function testBackendHealth() {
  log('=== 测试后端服务健康检查 ===');
  
  const result = await testAPI('/actuator/health');
  if (result) {
    assert(
      result.data.status === 'UP',
      '后端服务健康状态正常'
    );
  }
}

// 2. 测试学生登录
async function testStudentLogin() {
  log('=== 测试学生登录 ===');
  
  const loginData = {
    usernameOrEmail: TEST_USERS.student.username,
    password: TEST_USERS.student.password
  };
  
  const result = await testAPI('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(loginData)
  });
  
  if (result) {
    assert(
      result.data.success === true,
      '学生登录API返回成功状态'
    );
    
    assert(
      result.data.data && result.data.data.accessToken,
      '学生登录返回访问令牌'
    );
    
    assert(
      result.data.data.user && result.data.data.user.username === TEST_USERS.student.username,
      '学生登录返回正确的用户信息'
    );
    
    return result.data.data.accessToken;
  }
  
  return null;
}

// 3. 测试获取作业列表
async function testGetAssignments(token) {
  log('=== 测试获取作业列表 ===');
  
  const result = await testAPI('/api/submissions/my-submissions', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  if (result) {
    assert(
      result.response.status === 200,
      '获取作业列表API返回成功状态'
    );
    
    assert(
      Array.isArray(result.data),
      '作业列表返回数组格式'
    );
    
    assert(
      result.data.length > 0,
      '作业列表包含作业数据'
    );
    
    return result.data;
  }
  
  return [];
}

// 4. 测试获取作业结果
async function testGetAssignmentResult(token, assignments) {
  log('=== 测试获取作业结果 ===');
  
  if (assignments.length === 0) {
    log('没有作业可测试结果查看', 'WARN');
    return;
  }
  
  const assignment = assignments[0];
  // 测试获取作业结果
  const result = await testAPI(`/api/submissions/${assignment.id}/result`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  if (result) {
    assert(
      result.data.success === true,
      '获取作业结果API返回成功状态'
    );
    
    const resultData = result.data.data;
    assert(
      resultData && typeof resultData.totalScore === 'number',
      '作业结果包含总分信息'
    );
    
    assert(
      resultData && typeof resultData.teacherComment === 'string',
      '作业结果包含教师评语'
    );
    
    assert(
      resultData && Array.isArray(resultData.questionResults),
      '作业结果包含题目结果数组'
    );
  }
}

// 5. 测试教师登录
async function testTeacherLogin() {
  log('=== 测试教师登录 ===');
  
  const loginData = {
    usernameOrEmail: TEST_USERS.teacher.username,
    password: TEST_USERS.teacher.password
  };
  
  const result = await testAPI('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(loginData)
  });
  
  if (result) {
    assert(
      result.data.success === true,
      '教师登录API返回成功状态'
    );
    
    assert(
      result.data.data && result.data.data.accessToken,
      '教师登录返回访问令牌'
    );
    
    assert(
      result.data.data.user && result.data.data.user.roles.includes('ROLE_HQ_TEACHER'),
      '教师登录返回正确的角色信息'
    );
    
    return result.data.data.accessToken;
  }
  
  return null;
}

// 6. 测试错误场景
async function testErrorScenarios() {
  log('=== 测试错误场景 ===');
  
  // 测试错误的用户名密码
  const wrongCredentials = {
    usernameOrEmail: 'wrong_user',
    password: 'wrong_password'
  };
  
  const result = await testAPI('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(wrongCredentials)
  }, 401);
  
  if (result) {
    assert(
      result.data.success === false,
      '错误凭据登录返回失败状态'
    );
  }
  
  // 测试未授权访问
  const unauthorizedResult = await testAPI('/api/assignments', {
    method: 'GET'
  }, 401);
  
  if (unauthorizedResult) {
    assert(
      unauthorizedResult.response.status === 401,
      '未授权访问返回401状态码'
    );
  }
}

// 7. 测试JWT token验证
async function testJWTTokenValidation(token) {
  log('=== 测试JWT token验证 ===');
  
  // 测试有效token
  const validResult = await testAPI('/api/users/me', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  if (validResult) {
    assert(
      validResult.data.success === true,
      '有效token可以获取用户信息'
    );
  }
  
  // 测试无效token
  const invalidResult = await testAPI('/api/users/me', {
    method: 'GET',
    headers: {
      'Authorization': 'Bearer invalid_token'
    }
  }, 401);
  
  if (invalidResult) {
    assert(
      invalidResult.response.status === 401,
      '无效token返回401状态码'
    );
  }
}

// 主测试函数
async function runE2ETests() {
  log('🚀 开始执行完整的端到端测试');
  log('='.repeat(50));
  
  try {
    // 1. 后端健康检查
    await testBackendHealth();
    
    // 2. 学生登录测试
    const studentToken = await testStudentLogin();
    
    if (studentToken) {
      // 3. 获取作业列表
      const assignments = await testGetAssignments(studentToken);
      
      // 4. 获取作业结果
      await testGetAssignmentResult(studentToken, assignments);
      
      // 5. JWT token验证
      await testJWTTokenValidation(studentToken);
    }
    
    // 6. 教师登录测试
    const teacherToken = await testTeacherLogin();
    
    // 7. 错误场景测试
    await testErrorScenarios();
    
  } catch (error) {
    log(`测试执行出现异常: ${error.message}`, 'ERROR');
    testResults.failed++;
    testResults.errors.push(`测试执行异常: ${error.message}`);
  }
  
  // 输出测试结果
  log('='.repeat(50));
  log('📊 测试结果汇总');
  log(`✅ 通过: ${testResults.passed}`);
  log(`❌ 失败: ${testResults.failed}`);
  log(`📈 成功率: ${((testResults.passed / (testResults.passed + testResults.failed)) * 100).toFixed(2)}%`);
  
  if (testResults.errors.length > 0) {
    log('❌ 失败详情:');
    testResults.errors.forEach((error, index) => {
      log(`  ${index + 1}. ${error}`);
    });
  }
  
  log('🏁 端到端测试完成');
  
  // 返回测试是否全部通过
  return testResults.failed === 0;
}

// 如果直接运行此脚本
if (require.main === module) {
  runE2ETests().then(success => {
    process.exit(success ? 0 : 1);
  });
}

module.exports = { runE2ETests, testResults };