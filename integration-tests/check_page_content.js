const puppeteer = require('puppeteer');

async function checkPageContent() {
  const browser = await puppeteer.launch({ headless: false });
  const page = await browser.newPage();
  
  try {
    // 设置认证token到localStorage
    await page.goto('http://localhost:5173/');
    await page.evaluate(() => {
      localStorage.setItem('auth_token', 'dummy-token-for-testing');
    });
    
    // 验证token是否设置成功
    const tokenSet = await page.evaluate(() => {
      return localStorage.getItem('auth_token');
    });
    console.log('Token设置状态:', tokenSet);
    
    // 等待一段时间确保token设置生效
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // 导航到结果页面
    await page.goto('http://localhost:5173/submissions/770e8400-e29b-41d4-a716-446655440001/result');
    await new Promise(resolve => setTimeout(resolve, 5000)); // 等待更长时间让页面完全加载
    
    // 检查页面内容
    const pageContent = await page.content();
    console.log('页面HTML内容（前2000字符）:');
    console.log(pageContent.substring(0, 2000));
    console.log('\n=== 组件检查 ===');
    
    // 检查控制台错误
    const consoleLogs = [];
    page.on('console', msg => {
      consoleLogs.push(`${msg.type()}: ${msg.text()}`);
    });
    
    // 检查当前路由
    const currentUrl = page.url();
    const routerReady = await page.evaluate(() => {
      return window.location.pathname;
    });
    
    // 检查Vue应用状态
    const vueAppState = await page.evaluate(() => {
      const app = document.querySelector('#app');
      return {
        exists: !!app,
        innerHTML: app ? app.innerHTML.substring(0, 500) : null,
        hasVueInstance: !!window.__VUE__
      };
    });
    
    // 检查各种组件是否存在
    const checks = await page.evaluate(() => {
      return {
        videoPlayer: !!document.querySelector('.video-player'),
        submissionResult: !!document.querySelector('.submission-result'),
        videoContainer: !!document.querySelector('.video-container'),
        videoElement: !!document.querySelector('video'),
        iframe: !!document.querySelector('iframe'),
        questionResults: document.querySelectorAll('.question-result').length,
        allElements: document.querySelectorAll('*').length
      };
    });
    
    console.log('=== 路由和应用状态 ===');
    console.log('当前URL:', currentUrl);
    console.log('路由路径:', routerReady);
    console.log('Vue应用状态:', vueAppState);
    
    console.log('\n=== 控制台日志 ===');
    consoleLogs.forEach(log => console.log(log));
    
    console.log('\n=== 组件检查 ===');
    console.log('VideoPlayer组件存在:', checks.videoPlayer);
    console.log('SubmissionResult组件存在:', checks.submissionResult);
    console.log('视频容器存在:', checks.videoContainer);
    console.log('视频元素存在:', checks.videoElement);
    console.log('iframe元素存在:', checks.iframe);
    console.log('题目结果数量:', checks.questionResults);
    console.log('页面总元素数量:', checks.allElements);
    
    // 检查Vue应用状态
    const vueApp = await page.evaluate(() => {
      return !!document.querySelector('#app');
    });
    console.log('Vue应用容器存在:', vueApp);
    
  } catch (error) {
    console.error('检查页面内容时出错:', error);
  } finally {
    await browser.close();
  }
}

checkPageContent();