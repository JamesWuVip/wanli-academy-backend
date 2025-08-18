// 测试新的 /api/assignments/my-assignments API端点

const BASE_URL = 'http://localhost:8080';

const TEST_STUDENT = {
  username: 'test_student1',
  password: 'password123'
};

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
  console.log('🔐 学生登录测试...');
  
  const response = await makeRequest(`${BASE_URL}/api/auth/login`, {
    method: 'POST',
    body: JSON.stringify({
      usernameOrEmail: TEST_STUDENT.username,
      password: TEST_STUDENT.password
    })
  });
  
  if (response.status === 200 && response.data && response.data.data) {
    console.log('✅ 登录成功');
    console.log('Token:', response.data.data.accessToken.substring(0, 20) + '...');
    return response.data.data.accessToken;
  } else {
    console.log('❌ 登录失败:', response.status, response.data);
    return null;
  }
}

// 测试新的 my-assignments API
async function testMyAssignmentsAPI(token) {
  console.log('\n📚 测试 /api/assignments/my-assignments API...');
  
  const response = await makeRequest(`${BASE_URL}/api/assignments/my-assignments`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  console.log('状态码:', response.status);
  
  if (response.status === 200) {
    console.log('✅ API调用成功');
    console.log('响应数据:', JSON.stringify(response.data, null, 2));
    
    if (Array.isArray(response.data) && response.data.length > 0) {
      const assignment = response.data[0];
      console.log('\n📋 第一个作业信息:');
      console.log('- ID:', assignment.id);
      console.log('- 标题:', assignment.title);
      console.log('- 状态:', assignment.status);
      console.log('- 提交ID:', assignment.submissionId);
      console.log('- 得分:', assignment.score);
      console.log('- 最高分:', assignment.maxScore);
      
      if (assignment.submissionId) {
        console.log('✅ 包含submissionId字段，前端"查看结果"按钮应该可以显示');
      } else {
        console.log('⚠️  缺少submissionId字段');
      }
    } else {
      console.log('⚠️  作业列表为空');
    }
  } else {
    console.log('❌ API调用失败:', response.data);
  }
}

// 主测试函数
async function main() {
  console.log('🚀 开始测试新的 my-assignments API端点\n');
  
  const token = await loginStudent();
  if (token) {
    await testMyAssignmentsAPI(token);
  }
  
  console.log('\n✨ 测试完成');
}

// 运行测试
main().catch(console.error);