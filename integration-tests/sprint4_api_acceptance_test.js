const axios = require('axios');
const fs = require('fs');
const path = require('path');

// 测试配置
const config = {
  baseURL: 'http://localhost:8080/api',
  studentCredentials: {
    username: 'test_student1',
    password: 'password123'
  },
  timeout: 10000
};

// 测试结果存储
let testResults = [];
let testStartTime = Date.now();

// 辅助函数：记录测试结果
function recordTest(testName, passed, details = '', error = null) {
  const result = {
    testName,
    passed,
    details,
    error: error ? error.message : null,
    timestamp: new Date().toISOString()
  };
  testResults.push(result);
  
  const status = passed ? '✅' : '❌';
  console.log(`${status} ${testName}: ${details}`);
  if (error) {
    console.log(`   错误: ${error.message}`);
  }
}

// 辅助函数：HTTP请求
async function makeRequest(method, url, data = null, headers = {}) {
  try {
    const response = await axios({
      method,
      url: `${config.baseURL}${url}`,
      data,
      headers,
      timeout: config.timeout
    });
    return { success: true, data: response.data, status: response.status };
  } catch (error) {
    return { 
      success: false, 
      error: error.response ? error.response.data : error.message,
      status: error.response ? error.response.status : 0
    };
  }
}

// 测试1: 学员登录
async function testStudentLogin() {
  console.log('\n📋 测试1: 学员身份登录');
  
  const result = await makeRequest('POST', '/auth/login', {
    username: config.studentCredentials.username,
    password: config.studentCredentials.password
  });
  
  if (result.success && result.data.success && result.data.data.accessToken) {
    const token = result.data.data.accessToken;
    recordTest('学员身份登录', true, `成功获取访问令牌: ${token.substring(0, 20)}...`);
    return token;
  } else {
    recordTest('学员身份登录', false, '登录失败', new Error(result.error || '未知错误'));
    return null;
  }
}

// 测试2: 获取作业列表
async function testAssignmentList(token) {
  console.log('\n📋 测试2: 获取作业列表');
  
  const result = await makeRequest('GET', '/assignments', null, {
    'Authorization': `Bearer ${token}`
  });
  
  if (result.success && result.data.success && Array.isArray(result.data.data)) {
    const assignments = result.data.data;
    recordTest('获取作业列表', true, `成功获取 ${assignments.length} 个作业`);
    
    // 查找已批改的作业
    const gradedAssignments = assignments.filter(a => a.status === 'GRADED' || a.hasSubmission);
    if (gradedAssignments.length > 0) {
      recordTest('存在已批改作业', true, `找到 ${gradedAssignments.length} 个已批改作业`);
      return gradedAssignments[0]; // 返回第一个已批改作业
    } else {
      recordTest('存在已批改作业', false, '未找到已批改的作业');
      return assignments.length > 0 ? assignments[0] : null;
    }
  } else {
    recordTest('获取作业列表', false, '获取作业列表失败', new Error(result.error || '未知错误'));
    return null;
  }
}

// 测试3: 获取提交记录
async function testSubmissionRecords(token, assignmentId) {
  console.log('\n📋 测试3: 获取提交记录');
  
  const result = await makeRequest('GET', '/submissions/my-submissions', null, {
    'Authorization': `Bearer ${token}`
  });
  
  if (result.success && result.data.success && Array.isArray(result.data.data)) {
    const submissions = result.data.data;
    recordTest('获取提交记录', true, `成功获取 ${submissions.length} 个提交记录`);
    
    // 查找指定作业的提交记录
    const targetSubmission = submissions.find(s => s.assignmentId === assignmentId);
    if (targetSubmission) {
      recordTest('找到目标作业提交记录', true, `提交ID: ${targetSubmission.id}`);
      return targetSubmission;
    } else {
      recordTest('找到目标作业提交记录', false, '未找到指定作业的提交记录');
      return submissions.length > 0 ? submissions[0] : null;
    }
  } else {
    recordTest('获取提交记录', false, '获取提交记录失败', new Error(result.error || '未知错误'));
    return null;
  }
}

// 测试4: 获取作业结果详情
async function testSubmissionResult(token, submissionId) {
  console.log('\n📋 测试4: 获取作业结果详情');
  
  const result = await makeRequest('GET', `/submissions/${submissionId}/result`, null, {
    'Authorization': `Bearer ${token}`
  });
  
  if (result.success && result.data.success && result.data.data) {
    const resultData = result.data.data;
    recordTest('获取作业结果详情', true, '成功获取结果详情');
    
    // 验证总分
    if (typeof resultData.totalScore === 'number') {
      recordTest('结果包含总分', true, `总分: ${resultData.totalScore}`);
    } else {
      recordTest('结果包含总分', false, '结果中缺少总分信息');
    }
    
    // 验证教师评语
    if (resultData.teacherComment || resultData.teacherFeedback) {
      const feedback = resultData.teacherComment || resultData.teacherFeedback;
      recordTest('结果包含教师评语', true, `评语: ${feedback.substring(0, 50)}...`);
    } else {
      recordTest('结果包含教师评语', false, '结果中缺少教师评语');
    }
    
    // 验证答案对比
    if (Array.isArray(resultData.questionResults) && resultData.questionResults.length > 0) {
      recordTest('结果包含答案对比', true, `包含 ${resultData.questionResults.length} 道题的答案对比`);
      
      // 检查第一道题的详细信息
      const firstQuestion = resultData.questionResults[0];
      if (firstQuestion.studentAnswer !== undefined && firstQuestion.correctAnswer !== undefined) {
        recordTest('答案对比数据完整', true, '学生答案和标准答案都存在');
      } else {
        recordTest('答案对比数据完整', false, '答案对比数据不完整');
      }
    } else {
      recordTest('结果包含答案对比', false, '结果中缺少答案对比信息');
    }
    
    return resultData;
  } else {
    recordTest('获取作业结果详情', false, '获取结果详情失败', new Error(result.error || '未知错误'));
    return null;
  }
}

// 测试5: 验证视频内容
async function testVideoContent(token, assignmentId) {
  console.log('\n📋 测试5: 验证视频内容');
  
  // 尝试获取作业详情中的视频信息
  const result = await makeRequest('GET', `/assignments/${assignmentId}`, null, {
    'Authorization': `Bearer ${token}`
  });
  
  if (result.success && result.data.success && result.data.data) {
    const assignment = result.data.data;
    
    if (assignment.videoUrl || assignment.explanationVideoUrl) {
      const videoUrl = assignment.videoUrl || assignment.explanationVideoUrl;
      recordTest('作业包含讲解视频', true, `视频URL: ${videoUrl}`);
      
      // 验证视频URL是否可访问（简单的URL格式检查）
      if (videoUrl.startsWith('http') || videoUrl.includes('.mp4') || videoUrl.includes('video')) {
        recordTest('视频URL格式正确', true, '视频URL格式符合预期');
      } else {
        recordTest('视频URL格式正确', false, '视频URL格式可能有问题');
      }
    } else {
      recordTest('作业包含讲解视频', false, '作业中未找到视频URL');
    }
  } else {
    recordTest('验证视频内容', false, '获取作业详情失败', new Error(result.error || '未知错误'));
  }
}

// 生成测试报告
function generateReport() {
  const testEndTime = Date.now();
  const testDuration = Math.round((testEndTime - testStartTime) / 1000);
  
  const totalTests = testResults.length;
  const passedTests = testResults.filter(r => r.passed).length;
  const failedTests = totalTests - passedTests;
  const successRate = totalTests > 0 ? ((passedTests / totalTests) * 100).toFixed(2) : '0.00';
  
  const report = {
    summary: {
      totalTests,
      passedTests,
      failedTests,
      successRate: `${successRate}%`,
      testDuration: `${testDuration}秒`,
      conclusion: passedTests === totalTests ? '✅ 验收测试通过' : '❌ 验收测试未通过'
    },
    testResults,
    timestamp: new Date().toISOString()
  };
  
  // 保存JSON报告
  const reportsDir = path.join(__dirname, 'reports');
  if (!fs.existsSync(reportsDir)) {
    fs.mkdirSync(reportsDir, { recursive: true });
  }
  
  const jsonReportPath = path.join(reportsDir, 'sprint4_api_acceptance_report.json');
  fs.writeFileSync(jsonReportPath, JSON.stringify(report, null, 2));
  
  // 生成Markdown报告
  const markdownReport = `# Sprint 4 API验收测试报告

## 测试概要
- **总测试数**: ${totalTests}
- **通过测试**: ${passedTests}
- **失败测试**: ${failedTests}
- **成功率**: ${successRate}%
- **测试时长**: ${testDuration}秒
- **结论**: ${report.summary.conclusion}

## 详细测试结果

${testResults.map((test, index) => {
  const status = test.passed ? '✅' : '❌';
  let result = `### ${index + 1}. ${test.testName} ${status}\n\n`;
  result += `- **详情**: ${test.details}\n`;
  if (test.error) {
    result += `- **错误**: ${test.error}\n`;
  }
  result += `- **时间**: ${test.timestamp}\n\n`;
  return result;
}).join('')}

## 验收标准检查

基于Sprint 4设计文档的验收标准：

1. **学员身份登录** - ${testResults.find(t => t.testName === '学员身份登录')?.passed ? '✅ 通过' : '❌ 未通过'}
2. **查看作业列表** - ${testResults.find(t => t.testName === '获取作业列表')?.passed ? '✅ 通过' : '❌ 未通过'}
3. **查看已批改作业结果** - ${testResults.find(t => t.testName === '获取作业结果详情')?.passed ? '✅ 通过' : '❌ 未通过'}
4. **显示总分和教师评语** - ${testResults.find(t => t.testName === '结果包含总分')?.passed && testResults.find(t => t.testName === '结果包含教师评语')?.passed ? '✅ 通过' : '❌ 未通过'}
5. **逐题答案对比** - ${testResults.find(t => t.testName === '结果包含答案对比')?.passed ? '✅ 通过' : '❌ 未通过'}
6. **教师讲解视频** - ${testResults.find(t => t.testName === '作业包含讲解视频')?.passed ? '✅ 通过' : '❌ 未通过'}

---
*报告生成时间: ${new Date().toLocaleString('zh-CN')}*
`;
  
  const markdownReportPath = path.join(reportsDir, 'Sprint4_API_Acceptance_Test_Report.md');
  fs.writeFileSync(markdownReportPath, markdownReport);
  
  return report;
}

// 主测试函数
async function runAPIAcceptanceTest() {
  console.log('🚀 启动Sprint 4 API验收测试...');
  console.log(`📡 后端API地址: ${config.baseURL}`);
  
  try {
    // 测试1: 学员登录
    const token = await testStudentLogin();
    if (!token) {
      console.log('❌ 登录失败，无法继续后续测试');
      return;
    }
    
    // 测试2: 获取作业列表
    const assignment = await testAssignmentList(token);
    if (!assignment) {
      console.log('❌ 获取作业列表失败，无法继续后续测试');
      return;
    }
    
    // 测试3: 获取提交记录
    const submission = await testSubmissionRecords(token, assignment.id);
    if (!submission) {
      console.log('❌ 获取提交记录失败，无法继续后续测试');
      return;
    }
    
    // 测试4: 获取作业结果详情
    await testSubmissionResult(token, submission.id);
    
    // 测试5: 验证视频内容
    await testVideoContent(token, assignment.id);
    
  } catch (error) {
    console.error('❌ 测试过程中发生未预期的错误:', error);
    recordTest('测试执行', false, '测试过程中发生未预期的错误', error);
  }
  
  // 生成测试报告
  console.log('\n📊 生成测试报告...');
  const report = generateReport();
  
  console.log('\n============================================================');
  console.log('📋 Sprint 4 API验收测试报告');
  console.log('============================================================');
  console.log(`总测试数: ${report.summary.totalTests}`);
  console.log(`通过测试: ${report.summary.passedTests}`);
  console.log(`失败测试: ${report.summary.failedTests}`);
  console.log(`成功率: ${report.summary.successRate}`);
  console.log(`测试时长: ${report.summary.testDuration}`);
  console.log(`结论: ${report.summary.conclusion}`);
  console.log('============================================================');
  
  console.log('\n📄 详细报告已保存到:');
  console.log('- JSON: /Users/wujames/Downloads/wanliRepo/integration-tests/reports/sprint4_api_acceptance_report.json');
  console.log('- Markdown: /Users/wujames/Downloads/wanliRepo/integration-tests/reports/Sprint4_API_Acceptance_Test_Report.md');
}

// 运行测试
runAPIAcceptanceTest().catch(console.error);