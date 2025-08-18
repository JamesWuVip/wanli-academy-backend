const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');

const config = {
  frontendURL: 'http://localhost:5173',
  backendURL: 'http://localhost:8080',
  credentials: {
    username: 'student1',
    password: 'password123'
  }
};

async function debugVideoData() {
  const browser = await puppeteer.launch({ 
    headless: false,
    defaultViewport: null,
    args: ['--start-maximized']
  });
  
  const page = await browser.newPage();
  
  // 监听JavaScript错误
  page.on('pageerror', error => {
    console.log('页面JavaScript错误:', error.message);
  });
  
  page.on('console', msg => {
    if (msg.type() === 'error') {
      console.log('控制台错误:', msg.text());
    }
  });
  
  try {
    console.log('开始调试视频数据...');
    
    // 监听网络请求
    const apiResponses = [];
    page.on('response', async (response) => {
      const url = response.url();
      if (url.includes('/api/submissions/') && url.includes('/result')) {
        try {
          const responseData = await response.json();
          apiResponses.push({
            url: url,
            status: response.status(),
            data: responseData
          });
          console.log('捕获到API响应:', url);
        } catch (e) {
          console.log('解析API响应失败:', e.message);
        }
      }
    });
    
    // 访问首页
    await page.goto(config.frontendURL);
    await new Promise(resolve => setTimeout(resolve, 2000));
    
    // 登录
    console.log('开始登录...');
    await page.waitForSelector('#username', { timeout: 10000 });
    await page.type('#username', config.credentials.username);
    await page.type('#password', config.credentials.password);
    await page.click('.login-btn');
    
    // 等待登录成功
    await new Promise(resolve => setTimeout(resolve, 3000));
    console.log('登录后，当前URL:', page.url());
    
    // 如果还在首页，说明登录失败
    if (page.url() === config.frontendURL + '/') {
      console.log('登录可能失败，尝试点击学生账号快捷按钮');
      await page.click('.student-btn');
      await new Promise(resolve => setTimeout(resolve, 2000));
    }
    
    // 点击查看结果
    console.log('等待查看结果按钮...');
    await page.waitForSelector('[data-testid="view-result-btn"]', { timeout: 10000 });
    console.log('找到查看结果按钮，准备点击...');
    
    // 点击第一个查看结果按钮
    const resultButtons = await page.$$('[data-testid="view-result-btn"]');
    console.log('找到', resultButtons.length, '个查看结果按钮');
    
    if (resultButtons.length > 0) {
      // 监听页面导航
      const navigationPromise = page.waitForNavigation({ timeout: 10000 }).catch(() => null);
      
      await resultButtons[0].click();
      console.log('已点击第一个查看结果按钮');
      
      // 等待导航或超时
      const navigationResult = await navigationPromise;
      if (navigationResult) {
        console.log('导航成功');
      } else {
        console.log('导航超时或未发生');
      }
    } else {
      throw new Error('未找到查看结果按钮');
    }
    
    // 等待结果页面加载
    await new Promise(resolve => setTimeout(resolve, 3000));
    console.log('结果页面URL:', page.url());
    
    // 检查是否成功导航到结果页面
    if (!page.url().includes('/result')) {
      console.log('未能导航到结果页面，当前URL:', page.url());
      // 尝试等待更长时间
      await new Promise(resolve => setTimeout(resolve, 5000));
      console.log('等待后的URL:', page.url());
    }
    
    // 等待页面完全加载
    await new Promise(resolve => setTimeout(resolve, 3000));
    
    // 检查页面数据
    const pageData = await page.evaluate(() => {
      // 检查Vue应用数据
      const app = document.querySelector('#app').__vue_app__;
      const vueData = {};
      
      // 查找所有可能的视频相关元素
      const videoElements = {
        videoTags: document.querySelectorAll('video').length,
        iframes: document.querySelectorAll('iframe').length,
        videoPlayers: document.querySelectorAll('[data-testid*="video"], .video-player, .video-container').length,
        allTestIds: Array.from(document.querySelectorAll('[data-testid]')).map(el => el.getAttribute('data-testid'))
      };
      
      // 获取页面HTML结构
      const pageStructure = {
        title: document.title,
        url: window.location.href,
        bodyClasses: document.body.className,
        mainContent: document.querySelector('main') ? document.querySelector('main').innerHTML.substring(0, 1000) : 'No main element'
      };
      
      return {
        videoElements,
        pageStructure,
        vueData
      };
    });
    
    // 生成调试报告
    const debugReport = {
      timestamp: new Date().toISOString(),
      pageData,
      apiResponses,
      screenshots: []
    };
    
    // 截图
    const screenshotPath = path.join(__dirname, 'debug_screenshots', `video_debug_${Date.now()}.png`);
    if (!fs.existsSync(path.dirname(screenshotPath))) {
      fs.mkdirSync(path.dirname(screenshotPath), { recursive: true });
    }
    await page.screenshot({ path: screenshotPath, fullPage: true });
    debugReport.screenshots.push(screenshotPath);
    
    // 保存调试报告
    const reportPath = path.join(__dirname, 'debug_video_data_report.json');
    fs.writeFileSync(reportPath, JSON.stringify(debugReport, null, 2));
    
    console.log('\n=== 调试结果 ===');
    console.log('API响应数量:', apiResponses.length);
    console.log('视频元素统计:', pageData.videoElements);
    console.log('页面结构:', pageData.pageStructure.title);
    console.log('调试报告已保存到:', reportPath);
    console.log('截图已保存到:', screenshotPath);
    
    if (apiResponses.length > 0) {
      console.log('\n=== API响应详情 ===');
      apiResponses.forEach((response, index) => {
        console.log(`响应 ${index + 1}:`, response.url);
        console.log('状态码:', response.status);
        if (response.data && response.data.question) {
          console.log('题目数据:', {
            id: response.data.question.id,
            content: response.data.question.content,
            videoUrl: response.data.question.videoUrl || '无视频URL',
            hasVideoUrl: !!response.data.question.videoUrl
          });
        }
      });
    }
    
  } catch (error) {
    console.error('调试过程中发生错误:', error);
  } finally {
    await browser.close();
  }
}

debugVideoData().catch(console.error);