const puppeteer = require('puppeteer');
const fs = require('fs');

(async () => {
  const browser = await puppeteer.launch({ headless: false });
  const page = await browser.newPage();
  
  try {
    // 访问首页
    await page.goto('http://localhost:5173');
    await page.waitForSelector('#username', { timeout: 10000 });
    
    // 登录
    await page.type('#username', 'test_student1');
    await page.type('#password', 'password123');
    await page.click('.login-btn');
    
    // 等待登录成功
    await page.waitForNavigation({ waitUntil: 'networkidle0' });
    
    // 监听网络请求
    const responses = [];
    page.on('response', async (response) => {
      const url = response.url();
      if (url.includes('/api/submissions/') && url.includes('/result')) {
        try {
          const responseData = await response.json();
          responses.push({
            url: url,
            status: response.status(),
            data: responseData
          });
          console.log('API Response captured:', url);
        } catch (e) {
          console.log('Failed to parse response:', e.message);
        }
      }
    });
    
    // 点击查看结果按钮
    await page.waitForSelector('.view-result-btn', { timeout: 10000 });
    await page.click('.view-result-btn');
    
    // 等待API响应
    await page.waitForTimeout(3000);
    
    // 保存API响应数据
    const apiData = {
      timestamp: new Date().toISOString(),
      responses: responses
    };
    
    fs.writeFileSync('api_response_check.json', JSON.stringify(apiData, null, 2));
    console.log('API响应数据已保存到 api_response_check.json');
    
    // 检查页面上的视频元素
    const pageAnalysis = await page.evaluate(() => {
      const videoElements = document.querySelectorAll('video').length;
      const iframeElements = document.querySelectorAll('iframe').length;
      const videoPlayerComponents = document.querySelectorAll('[class*="video"], [data-testid*="video"]').length;
      
      // 获取页面HTML的一部分
      const bodyHTML = document.body.innerHTML.substring(0, 3000);
      
      return {
        videoElements,
        iframeElements,
        videoPlayerComponents,
        bodyHTML
      };
    });
    
    console.log('页面分析结果:', pageAnalysis);
    
    // 保存页面分析结果
    fs.writeFileSync('page_analysis.json', JSON.stringify(pageAnalysis, null, 2));
    
  } catch (error) {
    console.error('测试失败:', error);
  } finally {
    await browser.close();
  }
})();