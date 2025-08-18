// 使用Node.js 18+内置的fetch API

// 测试配置
const API_BASE_URL = 'http://localhost:8080/api';
const FRONTEND_URL = 'http://localhost:5173';

// 测试登录流程
async function testLoginFlow() {
  console.log('🚀 开始测试登录流程...');
  
  try {
    // 1. 测试登录API
    console.log('\n1. 测试登录API...');
    const loginResponse = await fetch(`${API_BASE_URL}/auth/login`, {
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
      throw new Error(`登录API失败: ${loginResponse.status} ${loginResponse.statusText}`);
    }
    
    const loginData = await loginResponse.json();
    console.log('✅ 登录API成功');
    console.log('   完整响应:', JSON.stringify(loginData, null, 2));
    console.log('   Token:', loginData.data.accessToken ? '已获取' : '未获取');
    console.log('   用户信息:', loginData.data.username ? `${loginData.data.username} (${loginData.data.roles[0]})` : '未获取');
    
    // 2. 测试获取作业列表API
    console.log('\n2. 测试作业列表API...');
    const assignmentsResponse = await fetch(`${API_BASE_URL}/assignments?page=1&pageSize=10`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${loginData.data.accessToken}`,
        'Content-Type': 'application/json',
      }
    });
    
    if (!assignmentsResponse.ok) {
      throw new Error(`作业列表API失败: ${assignmentsResponse.status} ${assignmentsResponse.statusText}`);
    }
    
    const assignmentsData = await assignmentsResponse.json();
    console.log('✅ 作业列表API成功');
    console.log('   作业数量:', assignmentsData.content ? assignmentsData.content.length : 0);
    
    // 3. 测试前端页面可访问性
    console.log('\n3. 测试前端页面可访问性...');
    const frontendResponse = await fetch(FRONTEND_URL);
    
    if (!frontendResponse.ok) {
      throw new Error(`前端页面不可访问: ${frontendResponse.status}`);
    }
    
    console.log('✅ 前端页面可访问');
    
    console.log('\n🎉 所有测试通过！登录流程应该正常工作。');
    console.log('\n📋 测试结果总结:');
    console.log('   ✅ 后端登录API正常');
    console.log('   ✅ JWT Token生成正常');
    console.log('   ✅ 认证后API访问正常');
    console.log('   ✅ 前端页面可访问');
    
    console.log('\n🔍 如果前端登录仍有问题，请检查:');
    console.log('   1. 浏览器开发者工具的网络请求');
    console.log('   2. 浏览器控制台的JavaScript错误');
    console.log('   3. Vue DevTools中的auth store状态');
    
  } catch (error) {
    console.error('❌ 测试失败:', error.message);
    process.exit(1);
  }
}

// 运行测试
testLoginFlow();