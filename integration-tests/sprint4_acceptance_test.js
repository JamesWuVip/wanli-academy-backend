// Sprint 4 验收测试 - 万里书院在线作业系统
// 测试学员查看已批改作业结果的完整流程

const BASE_URL = 'http://localhost:8080';
const FRONTEND_URL = 'http://localhost:5173';

// 测试数据
const TEST_STUDENT = {
  username: 'test_student1',
  password: 'password123'
};

const TEST_TEACHER = {
  username: 'test_teacher1', 
  password: 'password123'
};

// 测试结果统计
let testResults = {
  total: 0,
  passed: 0,
  failed: 0,
  details: []
};

// 辅助函数
function logTest(testName, passed, message = '') {
  testResults.total++;
  if (passed) {
    testResults.passed++;
    console.log(`✅ ${testName}`);
  } else {
    testResults.failed++;
    console.log(`❌ ${testName}: ${message}`);
  }
  testResults.details.push({ testName, passed, message });
}

// HTTP请求辅助函数
async function makeRequest(url, options = {}) {
  try {
    const response = await fetch(url, {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers
      },
      ...options
    });
    return {
      status: response.status,
      data: response.status !== 204 ? await response.json() : null,
      headers: response.headers
    };
  } catch (error) {
    return {
      status: 0,
      error: error.message
    };
  }
}

// 学生登录并获取token
async function loginStudent() {
  console.log('\n=== 学生登录测试 ===');
  
  const response = await makeRequest(`${BASE_URL}/api/auth/login`, {
    method: 'POST',
    body: JSON.stringify({
      usernameOrEmail: TEST_STUDENT.username,
      password: TEST_STUDENT.password
    })
  });
  
  logTest('学生登录API返回成功状态', response.status === 200, `状态码: ${response.status}`);
  
  if (response.status === 200 && response.data && response.data.data) {
    logTest('学生登录返回JWT token', !!response.data.data.accessToken, '缺少accessToken字段');
    logTest('学生登录返回用户信息', !!response.data.data.userId, '缺少userId字段');
    logTest('学生登录返回正确角色', Array.isArray(response.data.data.roles) && response.data.data.roles.includes('ROLE_STUDENT'), `角色: ${JSON.stringify(response.data.data.roles)}`);
    return response.data.data.accessToken;
  }
  
  return null;
}

// 获取学生的作业列表
async function getStudentAssignments(token) {
  console.log('\n=== 获取作业列表测试 ===');
  
  const response = await makeRequest(`${BASE_URL}/api/assignments/published`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  logTest('获取已发布作业API返回成功状态', response.status === 200, `状态码: ${response.status}`);
  
  if (response.status === 200 && response.data) {
    logTest('作业列表不为空', Array.isArray(response.data) && response.data.length > 0, '作业列表为空');
    
    if (response.data.length > 0) {
      const assignment = response.data[0];
      logTest('作业包含必要字段', 
        assignment.id && assignment.title && assignment.description,
        '缺少id、title或description字段'
      );
      return response.data;
    }
  }
  
  return [];
}

// 获取学生的提交记录
async function getStudentSubmissions(token, assignmentId) {
  console.log('\n=== 获取提交记录测试 ===');
  
  const response = await makeRequest(`${BASE_URL}/api/submissions/my-submissions`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  logTest('获取提交记录API返回成功状态', response.status === 200, `状态码: ${response.status}`);
  
  if (response.status === 200 && response.data) {
    logTest('提交记录不为空', Array.isArray(response.data) && response.data.length > 0, '提交记录为空');
    
    if (response.data.length > 0) {
      const submission = response.data[0];
      logTest('提交记录包含必要字段',
        submission.id && submission.status,
        '缺少id或status字段'
      );
      
      // 查找已批改的提交
      const gradedSubmission = response.data.find(s => s.status === 'GRADED');
      if (gradedSubmission) {
        logTest('存在已批改的提交', true);
        return gradedSubmission;
      } else {
        logTest('存在已批改的提交', false, '没有找到状态为GRADED的提交');
      }
    }
  }
  
  return null;
}

// 获取作业提交结果详情
async function getSubmissionResult(token, submissionId) {
  console.log('\n=== 获取作业结果详情测试 ===');
  
  const response = await makeRequest(`${BASE_URL}/api/submissions/${submissionId}/result`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  logTest('获取作业结果API返回成功状态', response.status === 200, `状态码: ${response.status}`);
  
  if (response.status === 200 && response.data) {
    const result = response.data;
    
    // 验证基本字段
    logTest('结果包含提交ID', !!result.submissionId || !!result.id, '缺少submissionId字段');
    logTest('结果包含作业标题', !!result.homeworkTitle || !!result.assignmentTitle, '缺少作业标题字段');
    logTest('结果包含总分', typeof result.score === 'number', `总分类型: ${typeof result.score}`);
    logTest('结果包含教师评语', typeof result.teacherFeedback === 'string', '缺少teacherFeedback字段');
    
    // 验证题目数组
    logTest('结果包含题目数组', Array.isArray(result.questions), '缺少questions数组');
    
    if (Array.isArray(result.questions) && result.questions.length > 0) {
      const question = result.questions[0];
      logTest('题目包含必要字段',
        question.questionId && question.content && 
        question.studentAnswer && question.standardAnswer,
        '题目缺少必要字段'
      );
      
      logTest('题目包含正确性标记', typeof question.isCorrect === 'boolean', '缺少isCorrect字段');
      logTest('题目包含文字解析', typeof question.explanation === 'string', '缺少explanation字段');
      logTest('题目包含视频URL', typeof question.videoUrl === 'string', '缺少videoUrl字段');
    }
    
    return result;
  }
  
  return null;
}

// 测试前端页面访问
async function testFrontendPages() {
  console.log('\n=== 前端页面访问测试 ===');
  
  try {
    // 测试首页访问
    const homeResponse = await fetch(FRONTEND_URL);
    logTest('前端首页可访问', homeResponse.status === 200, `状态码: ${homeResponse.status}`);
    
    // 测试登录页面
    const loginResponse = await fetch(`${FRONTEND_URL}/login`);
    logTest('登录页面可访问', loginResponse.status === 200, `状态码: ${loginResponse.status}`);
    
  } catch (error) {
    logTest('前端服务可访问', false, `错误: ${error.message}`);
  }
}

// 测试后端健康状态
async function testBackendHealth() {
  console.log('\n=== 后端服务健康检查 ===');
  
  const response = await makeRequest(`${BASE_URL}/actuator/health`);
  logTest('后端健康检查API可访问', response.status === 200, `状态码: ${response.status}`);
  
  if (response.status === 200 && response.data) {
    logTest('后端服务状态正常', response.data.status === 'UP', `状态: ${response.data.status}`);
  }
}

// 主测试函数
async function runSprint4AcceptanceTest() {
  console.log('🚀 开始 Sprint 4 验收测试');
  console.log('测试目标: 验证学员查看已批改作业结果的完整流程\n');
  
  // 1. 测试服务可用性
  await testBackendHealth();
  await testFrontendPages();
  
  // 2. 学生登录流程
  const studentToken = await loginStudent();
  if (!studentToken) {
    console.log('❌ 学生登录失败，终止测试');
    printTestSummary();
    return;
  }
  
  // 3. 获取作业列表
  const assignments = await getStudentAssignments(studentToken);
  if (assignments.length === 0) {
    console.log('❌ 无法获取作业列表，终止测试');
    printTestSummary();
    return;
  }
  
  // 4. 获取提交记录
  const assignment = assignments[0];
  const gradedSubmission = await getStudentSubmissions(studentToken, assignment.id);
  if (!gradedSubmission) {
    console.log('❌ 没有找到已批改的提交，终止测试');
    printTestSummary();
    return;
  }
  
  // 5. 获取作业结果详情
  const submissionResult = await getSubmissionResult(studentToken, gradedSubmission.id);
  if (!submissionResult) {
    console.log('❌ 无法获取作业结果详情，终止测试');
    printTestSummary();
    return;
  }
  
  // 6. 验证核心功能
  console.log('\n=== Sprint 4 核心功能验证 ===');
  logTest('完整学习闭环可用', 
    studentToken && assignments.length > 0 && gradedSubmission && submissionResult,
    '学习闭环中某个环节失败'
  );
  
  logTest('作业结果查看界面数据完整',
    submissionResult.score !== undefined && 
    submissionResult.teacherFeedback && 
    Array.isArray(submissionResult.questions),
    '结果界面缺少关键数据'
  );
  
  if (submissionResult.questions && submissionResult.questions.length > 0) {
    const hasAnswerComparison = submissionResult.questions.every(q => 
      q.studentAnswer && q.standardAnswer
    );
    logTest('答案对比功能可用', hasAnswerComparison, '部分题目缺少答案对比数据');
    
    const hasExplanations = submissionResult.questions.every(q => q.explanation);
    logTest('文字解析功能可用', hasExplanations, '部分题目缺少文字解析');
    
    const hasVideoUrls = submissionResult.questions.every(q => q.videoUrl);
    logTest('视频讲解功能可用', hasVideoUrls, '部分题目缺少视频URL');
  }
  
  printTestSummary();
}

// 打印测试总结
function printTestSummary() {
  console.log('\n' + '='.repeat(60));
  console.log('📊 Sprint 4 验收测试总结');
  console.log('='.repeat(60));
  console.log(`总测试数: ${testResults.total}`);
  console.log(`通过: ${testResults.passed} ✅`);
  console.log(`失败: ${testResults.failed} ❌`);
  console.log(`成功率: ${((testResults.passed / testResults.total) * 100).toFixed(2)}%`);
  
  if (testResults.failed > 0) {
    console.log('\n失败的测试:');
    testResults.details
      .filter(test => !test.passed)
      .forEach(test => {
        console.log(`  ❌ ${test.testName}: ${test.message}`);
      });
  }
  
  console.log('\n' + '='.repeat(60));
  
  // 验收标准判断
  const passRate = (testResults.passed / testResults.total) * 100;
  if (passRate >= 90) {
    console.log('🎉 Sprint 4 验收测试通过！系统已达到验收标准。');
  } else if (passRate >= 70) {
    console.log('⚠️  Sprint 4 验收测试部分通过，需要修复部分问题。');
  } else {
    console.log('❌ Sprint 4 验收测试未通过，需要重大修复。');
  }
  
  console.log('='.repeat(60));
}

// 运行测试
if (require.main === module) {
  runSprint4AcceptanceTest().catch(error => {
    console.error('测试执行出错:', error);
    process.exit(1);
  });
}

module.exports = {
  runSprint4AcceptanceTest,
  testResults
};