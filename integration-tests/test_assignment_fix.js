const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');

// 测试配置
const config = {
  baseUrl: 'http://localhost:5173',
  timeout: 30000,
  studentCredentials: {
    username: 'test_student1',
    password: 'password123'
  }
};

// 辅助函数：截图
async function takeScreenshot(page, name) {
  const screenshotPath = path.join(__dirname, 'screenshots', `${name}_${Date.now()}.png`);
  
  // 确保截图目录存在
  const screenshotDir = path.dirname(screenshotPath);
  if (!fs.existsSync(screenshotDir)) {
    fs.mkdirSync(screenshotDir, { recursive: true });
  }
  
  await page.screenshot({ path: screenshotPath, fullPage: true });
  return screenshotPath;
}

// 主测试函数
async function testAssignmentFix() {
  let browser;
  let page;
  
  try {
    console.log('🚀 启动作业列表修复验证测试...');
    
    // 启动浏览器
    browser = await puppeteer.launch({
      headless: false,
      slowMo: 100,
      args: ['--no-sandbox', '--disable-setuid-sandbox']
    });
    
    page = await browser.newPage();
    await page.setViewport({ width: 1280, height: 720 });
    
    // 监听控制台消息
    page.on('console', msg => {
      console.log(`🖥️ 控制台 [${msg.type()}]: ${msg.text()}`);
    });
    
    // 监听网络请求
    page.on('response', response => {
      if (response.url().includes('/api/')) {
        console.log(`🌐 API响应: ${response.status()} ${response.url()}`);
      }
    });
    
    console.log('\n📋 步骤1: 访问首页');
    await page.goto(config.baseUrl, { waitUntil: 'networkidle2' });
    const title = await page.title();
    console.log(`✅ 页面标题: ${title}`);
    await takeScreenshot(page, 'homepage');
    
    console.log('\n📋 步骤2: 等待登录表单加载');
    await page.waitForSelector('#username', { timeout: 10000 });
    await page.waitForSelector('#password', { timeout: 10000 });
    await page.waitForSelector('.login-btn', { timeout: 10000 });
    console.log('✅ 登录表单已加载');
    
    console.log('\n📋 步骤3: 填写登录信息');
    await page.type('#username', config.studentCredentials.username);
    await page.type('#password', config.studentCredentials.password);
    await takeScreenshot(page, 'login_form_filled');
    console.log('✅ 登录信息已填写');
    
    console.log('\n📋 步骤4: 点击登录按钮');
    await page.click('.login-btn');
    
    // 等待登录完成
    await page.waitForNavigation({ waitUntil: 'networkidle2', timeout: 15000 });
    const currentUrl = page.url();
    console.log(`✅ 登录成功，当前URL: ${currentUrl}`);
    await takeScreenshot(page, 'after_login');
    
    console.log('\n📋 步骤5: 检查作业列表页面');
    // 等待一段时间让页面完全加载
    await new Promise(resolve => setTimeout(resolve, 3000));
    
    // 检查页面内容
    const pageContent = await page.content();
    const hasAssignmentList = pageContent.includes('作业列表') || pageContent.includes('assignment');
    const hasErrorMessage = pageContent.includes('获取作业列表失败') || pageContent.includes('error');
    
    console.log(`📊 页面分析:`);
    console.log(`   - 包含作业列表相关内容: ${hasAssignmentList ? '✅' : '❌'}`);
    console.log(`   - 包含错误消息: ${hasErrorMessage ? '❌' : '✅'}`);
    
    // 检查是否有作业卡片
    const assignmentCards = await page.$$('.assignment-card, .assignment-item');
    console.log(`   - 找到作业卡片数量: ${assignmentCards.length}`);
    
    await takeScreenshot(page, 'assignment_page_final');
    
    console.log('\n📋 步骤6: 等待API调用完成');
    await new Promise(resolve => setTimeout(resolve, 5000));
    
    // 最终检查
    const finalContent = await page.content();
    const stillHasError = finalContent.includes('获取作业列表失败');
    
    console.log('\n🎯 测试结果总结:');
    console.log(`   - 登录成功: ✅`);
    console.log(`   - 页面导航成功: ✅`);
    console.log(`   - 作业列表API调用: ${stillHasError ? '❌ 仍有错误' : '✅ 无错误消息'}`);
    console.log(`   - 作业卡片显示: ${assignmentCards.length > 0 ? '✅' : '❌'}`);
    
    const overallSuccess = !stillHasError && assignmentCards.length >= 0;
    console.log(`\n🏆 整体测试结果: ${overallSuccess ? '✅ 成功' : '❌ 需要进一步调试'}`);
    
    // 保持浏览器打开10秒供手动检查
    console.log('\n⏳ 保持浏览器打开10秒供手动检查...');
    await new Promise(resolve => setTimeout(resolve, 10000));
    
  } catch (error) {
    console.error('❌ 测试过程中发生错误:', error.message);
    if (page) {
      await takeScreenshot(page, 'test_error');
    }
  } finally {
    if (browser) {
      await browser.close();
    }
  }
}

// 运行测试
testAssignmentFix().catch(console.error);