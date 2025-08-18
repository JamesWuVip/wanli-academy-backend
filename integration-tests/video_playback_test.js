const puppeteer = require('puppeteer');

// 配置
const config = {
  frontendURL: 'http://localhost:5173',
  studentCredentials: {
    username: 'test_student1',
    password: 'password123'
  },
  timeout: 30000,
  screenshotDir: './screenshots',
  reportDir: './reports'
};

// 确保目录存在
const fs = require('fs');
if (!fs.existsSync(config.screenshotDir)) {
  fs.mkdirSync(config.screenshotDir, { recursive: true });
}
if (!fs.existsSync(config.reportDir)) {
  fs.mkdirSync(config.reportDir, { recursive: true });
}

// 测试结果记录
const testResults = [];

function logTestResult(testName, status, message, screenshot = null) {
  const result = {
    testName,
    status,
    message,
    timestamp: new Date().toISOString(),
    screenshot
  };
  testResults.push(result);
  console.log(`[${status.toUpperCase()}] ${testName}: ${message}`);
}

async function takeScreenshot(page, name) {
  const screenshotPath = `${config.screenshotDir}/${name}_${Date.now()}.png`;
  await page.screenshot({ path: screenshotPath, fullPage: true });
  return screenshotPath;
}

async function waitForElementAndClick(page, selector, timeout = 10000) {
  try {
    await page.waitForSelector(selector, { timeout });
    await page.click(selector);
    return true;
  } catch (error) {
    console.log(`无法找到或点击元素 ${selector}: ${error.message}`);
    return false;
  }
}

async function waitForElementAndType(page, selector, text, timeout = 10000) {
  try {
    await page.waitForSelector(selector, { timeout });
    // 清空字段的正确方法
    await page.evaluate((sel) => {
      const element = document.querySelector(sel);
      if (element) {
        element.value = '';
      }
    }, selector);
    await page.type(selector, text);
    return true;
  } catch (error) {
    console.log(`无法找到或输入元素 ${selector}:`, error.message);
    return false;
  }
}

async function runVideoPlaybackTest() {
  console.log('=== 视频播放功能测试 ===');
  
  const browser = await puppeteer.launch({
    headless: false,
    defaultViewport: null,
    args: ['--start-maximized']
  });
  
  const page = await browser.newPage();
  
  try {
    // 1. 访问首页
    console.log('1. 访问首页...');
    await page.goto(config.frontendURL, { waitUntil: 'networkidle2' });
    await takeScreenshot(page, 'homepage');
    logTestResult('访问首页', 'PASSED', '成功访问首页');
    
    // 2. 用户登录
    console.log('2. 执行用户登录...');
    
    // 查找并填写用户名
     const usernameSuccess = await waitForElementAndType(page, '#username', config.studentCredentials.username);
      if (!usernameSuccess) {
        logTestResult('用户名输入', 'FAILED', '无法找到用户名输入框');
        return;
      }
      
      // 查找并填写密码
      const passwordSuccess = await waitForElementAndType(page, '#password', config.studentCredentials.password);
      if (!passwordSuccess) {
        logTestResult('密码输入', 'FAILED', '无法找到密码输入框');
        return;
      }
      
      
      // 点击登录按钮
      const loginSuccess = await waitForElementAndClick(page, '.login-btn');
     if (!loginSuccess) {
       logTestResult('登录按钮点击', 'FAILED', '无法找到或点击登录按钮');
       return;
     }
    
    // 等待登录完成
     await new Promise(resolve => setTimeout(resolve, 3000));
    await takeScreenshot(page, 'after_login');
    
    // 检查登录是否成功（URL变化或页面内容变化）
    let currentUrl = page.url();
    const pageContent = await page.content();
    
    // 如果还在首页，尝试点击学生账号快捷按钮
    if (currentUrl === config.frontendURL + '/') {
      console.log('登录可能失败，尝试点击学生账号快捷按钮');
      await page.click('.student-btn');
      await new Promise(resolve => setTimeout(resolve, 2000));
      currentUrl = page.url();
    }
    
    // 检查是否包含登录后的内容
    const isLoggedIn = pageContent.includes('作业列表') || 
                      pageContent.includes('我的作业') || 
                      currentUrl.includes('/assignments') ||
                      currentUrl.includes('/dashboard');
    
    console.log('登录后URL:', currentUrl);
    console.log('页面内容包含作业列表:', pageContent.includes('作业列表'));
    console.log('页面内容包含我的作业:', pageContent.includes('我的作业'));
    
    if (isLoggedIn) {
       logTestResult('用户登录', 'PASSED', `登录成功，当前URL: ${currentUrl}`);
     } else {
       console.log('页面内容预览:', pageContent.substring(0, 500));
       await takeScreenshot(page, 'login_failed');
       logTestResult('用户登录', 'FAILED', `登录失败，未检测到登录后的页面内容，当前URL: ${currentUrl}`);
       return;
     }
    
    // 3. 导航到作业列表
    console.log('3. 导航到作业列表...');
    
    // 如果不在作业列表页面，尝试导航
    if (!currentUrl.includes('/assignments')) {
      await page.goto(config.frontendURL + '/assignments', { waitUntil: 'networkidle2' });
      currentUrl = page.url();
    }
    
    await takeScreenshot(page, 'assignments_page');
    
    // 检查是否有作业列表内容
    const hasAssignments = await page.evaluate(() => {
      return document.body.textContent.includes('作业') || 
             document.body.textContent.includes('Assignment') ||
             document.querySelector('.assignment') !== null ||
             document.querySelector('[data-testid*="assignment"]') !== null;
    });
    
    if (hasAssignments) {
      logTestResult('作业列表加载', 'PASSED', '作业列表页面加载成功');
    } else {
      logTestResult('作业列表加载', 'FAILED', '作业列表页面内容未找到');
    }
    
    // 4. 点击查看结果按钮
    console.log('4. 查找并点击查看结果按钮...');
    
    // 查找并点击查看结果按钮
    const viewResultButtons = await page.$$('[data-testid="view-result-btn"]');
    if (viewResultButtons.length === 0) {
      logTestResult('查看结果按钮', 'FAILED', '未找到查看结果按钮');
      
      // 尝试直接导航到结果页面
      console.log('尝试直接导航到结果页面...');
      await page.goto('http://localhost:5173/submissions/1/result', { waitUntil: 'networkidle2' });
    } else {
      await viewResultButtons[0].click();
      console.log('已点击查看结果按钮');
      logTestResult('查看结果按钮', 'PASSED', '成功点击查看结果按钮');
    }
    
    // 等待页面加载
    await new Promise(resolve => setTimeout(resolve, 3000));
    const resultUrl = page.url();
    console.log('结果页面URL:', resultUrl);
    
    if (resultUrl.includes('/result') || resultUrl.includes('/submissions/')) {
      logTestResult('导航到结果页面', 'PASSED', '成功导航到结果页面');
    } else {
      logTestResult('导航到结果页面', 'FAILED', `导航失败，当前URL: ${resultUrl}`);
    }
    
    await takeScreenshot(page, 'result_page');
    
    // 5. 检查结果页面组件
    console.log('5. 检查结果页面组件...');
    
    const componentsExist = await page.evaluate(() => {
      const submissionResult = document.querySelector('[data-testid="submission-result"]') || 
                              document.querySelector('.submission-result') ||
                              document.body.textContent.includes('提交结果') ||
                              document.body.textContent.includes('Submission Result');
      
      const videoPlayer = document.querySelector('video') || 
                         document.querySelector('[data-testid="video-player"]') ||
                         document.querySelector('.video-player') ||
                         document.querySelector('iframe[src*="video"]');
      
      // 检查所有可能的视频相关元素
      const allVideoElements = {
        video: document.querySelectorAll('video').length,
        iframe: document.querySelectorAll('iframe').length,
        videoPlayer: document.querySelectorAll('[data-testid*="video"]').length,
        videoClass: document.querySelectorAll('[class*="video"]').length,
        mediaElements: document.querySelectorAll('audio, video, embed, object').length
      };
      
      // 获取页面的完整HTML结构
      const htmlStructure = document.documentElement.outerHTML;
      
      return {
        submissionResult: !!submissionResult,
        videoPlayer: !!videoPlayer,
        pageContent: document.body.textContent.substring(0, 500),
        allVideoElements,
        htmlSnippet: htmlStructure.substring(0, 2000) // 获取前2000个字符的HTML
      };
    });
    
    console.log('页面组件检查:', componentsExist);
    console.log('页面内容预览:', componentsExist.pageContent);
    console.log('所有视频相关元素:', componentsExist.allVideoElements);
    console.log('页面HTML片段:', componentsExist.htmlSnippet);
    
    if (componentsExist.submissionResult) {
      logTestResult('结果页面组件', 'PASSED', '结果页面组件已加载');
    } else {
      logTestResult('结果页面组件', 'FAILED', '结果页面组件未找到');
    }
    
    // 6. 视频播放器检测
    console.log('\n=== 步骤6: 视频播放器检测 ===');
    try {
      // 等待更长时间让Vue组件完全渲染
      await new Promise(resolve => setTimeout(resolve, 3000));
      
      // 获取页面HTML内容用于调试
      const pageContent = await page.content();
      console.log('页面是否包含VideoPlayer组件:', pageContent.includes('video-player'));
      console.log('页面是否包含视频讲解文本:', pageContent.includes('视频讲解'));
      
      // 检查各种视频相关元素
      const videoElements = await page.$$('video');
      const iframeElements = await page.$$('iframe');
      const videoPlayerComponents = await page.$$('.video-player');
      const videoContainers = await page.$$('.video-container');
      const noVideoElements = await page.$$('.no-video');
      const videoSections = await page.$$('.video-section');
      
      console.log(`找到 ${videoElements.length} 个 video 元素`);
      console.log(`找到 ${iframeElements.length} 个 iframe 元素`);
      console.log(`找到 ${videoPlayerComponents.length} 个 .video-player 组件`);
      console.log(`找到 ${videoContainers.length} 个 .video-container 容器`);
      console.log(`找到 ${noVideoElements.length} 个 .no-video 元素`);
      console.log(`找到 ${videoSections.length} 个 .video-section 元素`);
      
      // 检查是否有"暂无视频讲解"的提示
      if (noVideoElements.length > 0) {
        const noVideoText = await noVideoElements[0].evaluate(el => el.textContent);
        console.log('无视频提示文本:', noVideoText);
      }
      
      // 检查视频URL数据
      const videoUrlData = await page.evaluate(() => {
        // 尝试从Vue组件实例中获取数据
        const app = window.__VUE_APP__ || window.Vue;
        if (app) {
          console.log('Vue app found');
        }
        
        // 检查页面中是否有videoUrl相关的数据
        const scripts = Array.from(document.querySelectorAll('script'));
        for (const script of scripts) {
          if (script.textContent && script.textContent.includes('videoUrl')) {
            return script.textContent;
          }
        }
        return null;
      });
      
      if (videoUrlData) {
        console.log('找到videoUrl相关数据');
      }
      
      // 判断测试结果
      if (videoElements.length > 0 || iframeElements.length > 0) {
        logTestResult('视频播放器检测', 'PASSED', `找到视频播放器: ${videoElements.length} video, ${iframeElements.length} iframe`);
        
        // 测试视频功能
        if (videoElements.length > 0) {
          console.log('测试HTML5视频播放功能...');
          const video = videoElements[0];
          const videoSrc = await video.evaluate(el => el.src);
          console.log('视频源:', videoSrc);
          
          try {
            await video.evaluate(v => v.play());
            console.log('视频播放成功');
          } catch (playError) {
            console.log('视频播放失败:', playError.message);
          }
        }
        
        if (iframeElements.length > 0) {
          console.log('检测到YouTube视频iframe...');
          const iframe = iframeElements[0];
          const iframeSrc = await iframe.evaluate(el => el.src);
          console.log('iframe源:', iframeSrc);
        }
      } else if (videoPlayerComponents.length > 0) {
        if (noVideoElements.length > 0) {
          logTestResult('视频播放器检测', 'PARTIAL', '视频播放器组件存在但显示"暂无视频讲解"');
        } else {
          logTestResult('视频播放器检测', 'PARTIAL', '视频播放器组件存在但未找到实际视频元素');
        }
      } else {
        logTestResult('视频播放器检测', 'FAILED', '视频播放器组件未找到');
      }
      
      await takeScreenshot(page, 'video_player_test');
    } catch (error) {
      console.error('视频播放器检测失败:', error);
      logTestResult('视频播放器检测', 'FAILED', `检测失败: ${error.message}`);
    }
    
  } catch (error) {
    console.error('测试过程中出现错误:', error);
    logTestResult('整体测试', 'FAILED', `测试异常: ${error.message}`);
  } finally {
    await takeScreenshot(page, 'final_state');
    await browser.close();
    
    // 生成测试报告
    generateTestReport();
  }
}

// 生成测试报告
function generateTestReport() {
  console.log('\n=== 测试报告 ===');
  
  const passed = testResults.filter(r => r.status === 'PASSED').length;
  const failed = testResults.filter(r => r.status === 'FAILED').length;
  const partial = testResults.filter(r => r.status === 'PARTIAL').length;
  
  console.log(`总测试数: ${testResults.length}`);
  console.log(`通过: ${passed}`);
  console.log(`失败: ${failed}`);
  console.log(`部分通过: ${partial}`);
  
  console.log('\n详细结果:');
  testResults.forEach(result => {
    console.log(`[${result.status}] ${result.testName}: ${result.message}`);
  });
  
  // 保存报告到文件
  const reportPath = `${config.reportDir}/video_test_report_${Date.now()}.json`;
  fs.writeFileSync(reportPath, JSON.stringify({
    summary: { total: testResults.length, passed, failed, partial },
    results: testResults,
    timestamp: new Date().toISOString()
  }, null, 2));
  
  console.log(`\n报告已保存到: ${reportPath}`);
}

// 运行测试
runVideoPlaybackTest().catch(console.error);