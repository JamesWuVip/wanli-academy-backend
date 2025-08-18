const puppeteer = require('puppeteer');

(async () => {
  const browser = await puppeteer.launch({ 
    headless: false,
    defaultViewport: null,
    args: ['--start-maximized']
  });
  
  const page = await browser.newPage();
  
  // 监听所有控制台消息
  page.on('console', msg => {
    const type = msg.type();
    const text = msg.text();
    console.log(`🖥️ 控制台 [${type}]: ${text}`);
  });
  
  // 监听页面错误
  page.on('pageerror', error => {
    console.log(`❌ 页面错误: ${error.message}`);
  });
  
  // 监听网络请求
  page.on('response', response => {
    if (response.url().includes('/api/')) {
      console.log(`🌐 API响应: ${response.status()} ${response.url()}`);
    }
  });
  
  try {
    // 访问首页（登录页面）
    await page.goto('http://localhost:5173/');
    
    // 等待登录表单加载
    await page.waitForSelector('#username', { timeout: 10000 });
    
    // 填写登录信息
    await page.type('#username', 'test_student1');
    await page.type('#password', 'password123');
    
    // 点击登录按钮
    await page.click('button[type="submit"]');
    
    // 等待登录成功并跳转
    await page.waitForNavigation({ waitUntil: 'networkidle0', timeout: 10000 });
    console.log(`✅ 登录成功，当前URL: ${page.url()}`);
    
    // 导航到作业列表页面
    await page.goto('http://localhost:5173/assignments');
    await page.waitForSelector('.assignment-list', { timeout: 10000 });
    
    console.log('⏳ 等待作业列表加载...');
    
    // 等待一段时间让API调用完成
    await new Promise(resolve => setTimeout(resolve, 5000));
    
    // 检查是否有错误信息显示
    const errorElements = await page.$$('.error-message, .error, [class*="error"]');
    if (errorElements.length > 0) {
      console.log('❌ 发现错误元素:');
      for (let i = 0; i < errorElements.length; i++) {
        const errorText = await page.evaluate(el => el.textContent, errorElements[i]);
        console.log(`   ${i + 1}. ${errorText}`);
      }
    }
    
    // 检查网络请求的响应内容
    const response = await page.evaluate(async () => {
      try {
        const res = await fetch('/api/assignments/my-assignments?page=1&pageSize=10', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        const data = await res.json();
        return { status: res.status, data };
      } catch (error) {
        return { error: error.message };
      }
    });
    
    console.log('📊 API响应数据:', JSON.stringify(response, null, 2));
    
    // 检查Vue组件的状态
    const vueState = await page.evaluate(() => {
      const app = document.querySelector('#app').__vue_app__;
      if (app) {
        const instance = app._instance;
        if (instance && instance.ctx) {
          return {
            assignments: instance.ctx.assignments,
            loading: instance.ctx.loading,
            error: instance.ctx.error
          };
        }
      }
      return null;
    });
    
    console.log('🔧 Vue组件状态:', JSON.stringify(vueState, null, 2));
    
    console.log('🔍 页面调试完成，10秒后自动关闭浏览器...');
    await new Promise(resolve => setTimeout(resolve, 10000));
    
  } catch (error) {
    console.error('❌ 调试过程中出现错误:', error.message);
  } finally {
    await browser.close();
  }
})();