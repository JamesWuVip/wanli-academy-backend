const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');

const config = {
  frontendURL: 'http://localhost:5173',
  studentCredentials: {
    username: 'test_student1',
    password: 'password123'
  }
};

async function debugResultPage() {
  const browser = await puppeteer.launch({ 
    headless: false,
    defaultViewport: { width: 1280, height: 720 },
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });
  
  const page = await browser.newPage();
  
  try {
    console.log('1. 访问首页...');
    await page.goto(config.frontendURL, { waitUntil: 'networkidle2' });
    
    console.log('2. 执行登录...');
    // 等待登录表单加载
    await page.waitForSelector('#username', { timeout: 10000 });
    
    // 清空并填写用户名
    await page.click('#username');
    await page.evaluate(() => document.querySelector('#username').value = '');
    await page.type('#username', config.studentCredentials.username, { delay: 50 });
    
    // 清空并填写密码
    await page.click('#password');
    await page.evaluate(() => document.querySelector('#password').value = '');
    await page.type('#password', config.studentCredentials.password, { delay: 50 });
    
    // 点击登录按钮
    await page.click('.login-btn');
    await new Promise(resolve => setTimeout(resolve, 3000));
    
    // 验证登录是否成功
    const loginSuccess = await page.evaluate(() => {
      const content = document.body.textContent;
      const url = window.location.href;
      return {
        hasAssignmentContent: content.includes('作业列表') || content.includes('我的作业'),
        currentUrl: url,
        isAssignmentPage: url.includes('/assignments') || url.includes('/dashboard')
      };
    });
    
    console.log('登录验证结果:', loginSuccess);
    
    if (!loginSuccess.hasAssignmentContent && !loginSuccess.isAssignmentPage) {
      throw new Error('登录失败，未能进入作业列表页面');
    }
    
    console.log('3. 点击查看结果按钮...');
    const selectors = [
      '[data-testid="view-result-btn"]',
      '.view-result-btn',
      'button:contains("📊 查看结果")',
      'button:contains("查看结果")',
      'a:contains("查看结果")',
      '[href*="result"]',
      'button[class*="result"]'
    ];
    
    let clicked = false;
    for (const selector of selectors) {
      try {
        await page.waitForSelector(selector, { timeout: 2000 });
        await page.click(selector);
        console.log(`成功点击查看结果按钮: ${selector}`);
        clicked = true;
        break;
      } catch (e) {
        console.log(`选择器 ${selector} 未找到或点击失败`);
      }
    }
    
    if (!clicked) {
      console.log('所有选择器都失败，尝试通过文本查找按钮...');
      const buttons = await page.$$('button, a');
      for (const button of buttons) {
        const text = await page.evaluate(el => el.textContent, button);
        if (text && (text.includes('查看结果') || text.includes('结果'))) {
          await button.click();
          console.log(`通过文本找到并点击按钮: ${text}`);
          clicked = true;
          break;
        }
      }
    }
    
    await new Promise(resolve => setTimeout(resolve, 3000));
    
    console.log('4. 分析结果页面结构...');
    const pageInfo = await page.evaluate(() => {
      const allElements = document.querySelectorAll('*');
      const elementTypes = {};
      
      Array.from(allElements).forEach(el => {
        const tagName = el.tagName.toLowerCase();
        elementTypes[tagName] = (elementTypes[tagName] || 0) + 1;
      });
      
      return {
        url: window.location.href,
        title: document.title,
        bodyText: document.body.textContent.substring(0, 1000),
        elementCounts: elementTypes,
        videoElements: {
          video: document.querySelectorAll('video').length,
          iframe: document.querySelectorAll('iframe').length,
          embed: document.querySelectorAll('embed').length,
          object: document.querySelectorAll('object').length,
          videoTestId: document.querySelectorAll('[data-testid*="video"]').length,
          videoClass: document.querySelectorAll('[class*="video"]').length
        },
        htmlStructure: document.documentElement.outerHTML.substring(0, 3000)
      };
    });
    
    console.log('=== 页面调试信息 ===');
    console.log('URL:', pageInfo.url);
    console.log('标题:', pageInfo.title);
    console.log('页面文本内容:', pageInfo.bodyText);
    console.log('元素统计:', pageInfo.elementCounts);
    console.log('视频相关元素:', pageInfo.videoElements);
    console.log('HTML结构片段:', pageInfo.htmlStructure);
    
    // 保存调试信息到文件
    const debugInfo = {
      timestamp: new Date().toISOString(),
      pageInfo
    };
    
    const debugFile = path.join(__dirname, 'debug_result_page_info.json');
    fs.writeFileSync(debugFile, JSON.stringify(debugInfo, null, 2));
    console.log('调试信息已保存到:', debugFile);
    
  } catch (error) {
    console.error('调试过程中出错:', error);
  } finally {
    await browser.close();
  }
}

debugResultPage().catch(console.error);