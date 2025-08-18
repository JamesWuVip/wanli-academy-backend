const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');

// 配置
const config = {
  frontendUrl: 'http://localhost:5174',
  backendUrl: 'http://localhost:8080',
  screenshotsDir: './integration-tests/screenshots'
};

// 确保截图目录存在
if (!fs.existsSync(config.screenshotsDir)) {
  fs.mkdirSync(config.screenshotsDir, { recursive: true });
}

async function debugSubmissionResultPage() {
  let browser;
  let page;
  
  try {
    console.log('🚀 启动浏览器调试...');
    
    browser = await puppeteer.launch({
      headless: false, // 显示浏览器窗口
      devtools: true,  // 打开开发者工具
      args: ['--no-sandbox', '--disable-setuid-sandbox']
    });
    
    page = await browser.newPage();
    
    // 监听控制台消息
    page.on('console', msg => {
      console.log(`🖥️  控制台 [${msg.type()}]:`, msg.text());
    });
    
    // 监听网络请求
    page.on('request', request => {
      if (request.url().includes('/api/')) {
        console.log(`📡 API请求: ${request.method()} ${request.url()}`);
      }
    });
    
    // 监听网络响应
    page.on('response', response => {
      if (response.url().includes('/api/')) {
        console.log(`📡 API响应: ${response.status()} ${response.url()}`);
      }
    });
    
    // 监听页面错误
    page.on('pageerror', error => {
      console.log(`❌ 页面错误:`, error.message);
    });
    
    console.log('🌐 访问首页...');
    await page.goto(config.frontendUrl, { waitUntil: 'networkidle2' });
    
    // 登录
    console.log('🔐 执行登录...');
    await page.type('#username', 'test_student1');
    await page.type('#password', 'password123');
    await page.click('button[type="submit"]');
    
    // 等待登录完成
    await new Promise(resolve => setTimeout(resolve, 3000));
    
    // 直接访问作业结果页面
    const submissionId = '770e8400-e29b-41d4-a716-446655440001';
    const resultUrl = `${config.frontendUrl}/submissions/${submissionId}/result`;
    
    console.log(`📄 直接访问作业结果页面: ${resultUrl}`);
    await page.goto(resultUrl, { waitUntil: 'networkidle2' });
    
    // 等待页面加载
    await new Promise(resolve => setTimeout(resolve, 5000));
    
    // 截图
    const screenshotPath = path.join(config.screenshotsDir, `debug_result_page_${Date.now()}.png`);
    await page.screenshot({ path: screenshotPath, fullPage: true });
    console.log(`📸 截图保存到: ${screenshotPath}`);
    
    // 获取页面内容
    const pageContent = await page.content();
    
    // 检查关键元素
    console.log('\n🔍 检查页面元素:');
    
    // 检查是否有加载状态
    const hasLoading = pageContent.includes('正在加载');
    console.log(`加载状态: ${hasLoading ? '✅' : '❌'}`);
    
    // 检查是否有错误信息
    const hasError = pageContent.includes('错误') || pageContent.includes('失败');
    console.log(`错误信息: ${hasError ? '❌ 有错误' : '✅ 无错误'}`);
    
    // 检查总分
    const hasScore = pageContent.includes('总分') || pageContent.includes('85/100');
    console.log(`总分显示: ${hasScore ? '✅' : '❌'}`);
    
    // 检查教师评语
    const hasComment = pageContent.includes('教师评语') || pageContent.includes('计算正确');
    console.log(`教师评语: ${hasComment ? '✅' : '❌'}`);
    
    // 检查作业标题
    const hasTitle = pageContent.includes('数学基础练习');
    console.log(`作业标题: ${hasTitle ? '✅' : '❌'}`);
    
    // 获取Vue组件数据
    const componentData = await page.evaluate(() => {
      // 尝试获取Vue实例数据
      const app = document.querySelector('#app').__vue_app__;
      if (app) {
        const instance = app._instance;
        if (instance && instance.data) {
          return {
            loading: instance.data.loading,
            error: instance.data.error,
            submissionResult: instance.data.submissionResult
          };
        }
      }
      return null;
    });
    
    if (componentData) {
      console.log('\n📊 Vue组件数据:');
      console.log('Loading:', componentData.loading);
      console.log('Error:', componentData.error);
      console.log('SubmissionResult:', componentData.submissionResult ? '有数据' : '无数据');
    }
    
    // 保持浏览器打开以便手动检查
    console.log('\n🔍 浏览器将保持打开状态，请手动检查页面...');
    console.log('按 Ctrl+C 退出');
    
    // 等待用户手动关闭
    await new Promise(() => {});
    
  } catch (error) {
    console.error('❌ 调试过程中出现错误:', error);
  } finally {
    if (browser) {
      await browser.close();
    }
  }
}

// 运行调试
debugSubmissionResultPage().catch(console.error);