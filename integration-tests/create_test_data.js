const axios = require('axios');

// 配置
const API_BASE = 'http://localhost:8080/api';
const TEST_USER = {
  username: 'test_student1',
  password: 'password123'
};

async function createTestData() {
  try {
    console.log('🔧 创建测试数据...');
    
    // 1. 登录获取token
    console.log('📋 步骤1: 用户登录');
    const loginResponse = await axios.post(`${API_BASE}/auth/login`, {
      usernameOrEmail: TEST_USER.username,
      password: TEST_USER.password
    });
    
    const token = loginResponse.data.data.accessToken;
    console.log('✅ 登录成功');
    
    const headers = {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    };
    
    // 2. 获取已发布的作业列表
    console.log('📋 步骤2: 获取已发布作业列表');
    const assignmentsResponse = await axios.get(`${API_BASE}/assignments/published`, { headers });
    console.log('📋 API响应:', JSON.stringify(assignmentsResponse.data, null, 2));
    const assignments = assignmentsResponse.data.data || assignmentsResponse.data || [];
    console.log(`✅ 找到 ${assignments.length} 个作业`);
    
    // 3. 为前两个作业创建提交记录并设置为已批改状态
    for (let i = 0; i < Math.min(2, assignments.length); i++) {
      const assignment = assignments[i];
      console.log(`📋 步骤${3+i}: 处理作业 "${assignment.title}"`);
      
      try {
        // 创建提交记录
        const submissionData = {
          assignmentId: assignment.id,
          answers: [
            {
              questionId: 'q1',
              answer: '这是学生的答案1',
              questionType: 'TEXT'
            },
            {
              questionId: 'q2', 
              answer: '这是学生的答案2',
              questionType: 'TEXT'
            }
          ]
        };
        
        const submitResponse = await axios.post(`${API_BASE}/submissions`, submissionData, { headers });
        const submissionId = submitResponse.data.data.id;
        console.log(`✅ 创建提交记录: ${submissionId}`);
        
        // 模拟教师批改（这需要管理员权限，我们先跳过）
        console.log(`⚠️  作业 "${assignment.title}" 已提交，需要教师批改后才能查看结果`);
        
      } catch (error) {
        console.log(`⚠️  作业 "${assignment.title}" 可能已经提交过了`);
      }
    }
    
    console.log('✅ 测试数据创建完成');
    
  } catch (error) {
    console.error('❌ 创建测试数据失败:', error.response?.data || error.message);
  }
}

// 运行脚本
if (require.main === module) {
  createTestData();
}

module.exports = { createTestData };