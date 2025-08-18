const puppeteer = require('puppeteer');

async function testNavigation() {
  const browser = await puppeteer.launch({ headless: false, devtools: true });
  const page = await browser.newPage();
  
  try {
    console.log('1. 访问首页...');
    await page.goto('http://localhost:5173/');
    await page.waitForSelector('#app', { timeout: 10000 });
    
    console.log('2. 设置认证token...');
    await page.evaluate(() => {
      localStorage.setItem('auth_token', 'dummy-token-for-testing');
    });
    
    const token = await page.evaluate(() => localStorage.getItem('auth_token'));
    console.log('Token设置成功:', token);
    
    console.log('3. 尝试导航到结果页面...');
    const targetUrl = 'http://localhost:5173/submissions/770e8400-e29b-41d4-a716-446655440001/result';
    
    // 监听页面导航事件
    page.on('response', response => {
      console.log('响应:', response.url(), response.status());
    });
    
    page.on('console', msg => {
      console.log('控制台:', msg.text());
    });
    
    await page.goto(targetUrl);
    await new Promise(resolve => setTimeout(resolve, 3000));
    
    const currentUrl = page.url();
    const currentPath = await page.evaluate(() => window.location.pathname);
    
    console.log('4. 导航结果:');
    console.log('目标URL:', targetUrl);
    console.log('当前URL:', currentUrl);
    console.log('当前路径:', currentPath);
    
    // 检查页面内容
    const pageTitle = await page.title();
    const hasSubmissionResult = await page.$('.submission-result') !== null;
    const hasVideoPlayer = await page.$('.video-player') !== null;
    
    console.log('页面标题:', pageTitle);
    console.log('SubmissionResult组件存在:', hasSubmissionResult);
    console.log('VideoPlayer组件存在:', hasVideoPlayer);
    
    // 等待用户观察
    console.log('\n浏览器将保持打开状态，请手动检查页面...');
    await new Promise(resolve => setTimeout(resolve, 30000));
    
  } catch (error) {
    console.error('测试导航时出错:', error);
  } finally {
    await browser.close();
  }
}

testNavigation();