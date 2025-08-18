// 使用Node.js 18+内置的fetch API

const BASE_URL = 'http://localhost:8080/api';

async function testSubmissionResultAPI() {
  try {
    // 1. 登录获取token
    console.log('正在登录...');
    const loginResponse = await fetch(`${BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        usernameOrEmail: 'test_student1',
        password: 'password123'
      })
    });

    if (!loginResponse.ok) {
      const errorText = await loginResponse.text();
      throw new Error(`Login failed: ${errorText}`);
    }

    const loginData = await loginResponse.json();
    console.log('登录响应数据:', JSON.stringify(loginData, null, 2));
    const token = loginData.data?.accessToken || loginData.token || loginData.accessToken;
    console.log('提取的token:', token);
    
    if (!token) {
      throw new Error('未能从登录响应中获取token');
    }
    
    console.log('登录成功，获取到token');

    // 2. 调用作业结果API
    console.log('正在获取作业结果...');
    const submissionId = '770e8400-e29b-41d4-a716-446655440001'; // 使用测试中实际访问的ID
    const resultResponse = await fetch(`${BASE_URL}/submissions/${submissionId}/result`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    });

    console.log('API响应状态码:', resultResponse.status);
    
    if (!resultResponse.ok) {
      const errorText = await resultResponse.text();
      throw new Error(`API call failed: ${errorText}`);
    }

    const resultData = await resultResponse.json();
    console.log('\n=== 后端API返回的原始数据 ===');
    console.log(JSON.stringify(resultData, null, 2));
    
    // 3. 测试前端API的数据转换
    console.log('\n=== 前端API转换后的数据格式 ===');
    const transformedData = {
      submissionId: resultData.submissionId,
      homeworkId: resultData.assignmentId, // 后端返回assignmentId，前端期望homeworkId
      homeworkTitle: resultData.assignmentTitle, // 后端返回assignmentTitle，前端期望homeworkTitle
      studentId: resultData.studentId,
      studentName: resultData.studentUsername, // 后端返回studentUsername，前端期望studentName
      totalScore: resultData.score, // 后端返回score，前端期望totalScore
      maxScore: resultData.maxScore,
      teacherComment: resultData.feedback, // 后端返回feedback，前端期望teacherComment
      teacherFeedback: resultData.teacherFeedback,
      submittedAt: resultData.submittedAt,
      gradedAt: resultData.gradedAt,
      questions: resultData.questions || [] // 确保questions是数组
    };
    
    console.log(JSON.stringify(transformedData, null, 2));
    
    // 4. 验证关键字段
    console.log('\n=== 字段验证 ===');
    console.log('totalScore (前端期望):', transformedData.totalScore);
    console.log('maxScore:', transformedData.maxScore);
    console.log('teacherComment (前端期望):', transformedData.teacherComment);
    console.log('homeworkTitle (前端期望):', transformedData.homeworkTitle);
    console.log('questions数组长度:', transformedData.questions.length);
    
  } catch (error) {
    console.error('测试失败:', error.message);
  }
}

testSubmissionResultAPI();