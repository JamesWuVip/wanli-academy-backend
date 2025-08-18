const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');

// 配置
const config = {
  baseUrl: 'http://localhost:5173',
  backendUrl: 'http://localhost:8080',
  studentCredentials: {
    username: 'test_student1',
    password: 'password123'
  },
  timeout: 30000
};

// 等待元素并输入文本
async function waitForElementAndType(page, selector, text, timeout = 10000) {
  try {
    await page.waitForSelector(selector, { timeout });
    await page.type(selector, text);
    return true;
  } catch (error) {
    console.error(`无法找到或输入到元素 ${selector}:`, error.message);
    return false;
  }
}

// 等待元素并点击
async function waitForElementAndClick(page, selector, timeout = 10000) {
  try {
    await page.waitForSelector(selector, { timeout });
    await page.click(selector);
    return true;
  } catch (error) {
    console.error(`无法找到或点击元素 ${selector}:`, error.message);
    return false;
  }
}

// 截图函数
async function takeScreenshot(page, name) {
  const screenshotDir = path.join(__dirname, 'debug_screenshots');
  if (!fs.existsSync(screenshotDir)) {
    fs.mkdirSync(screenshotDir, { recursive: true });
  }
  
  const timestamp = Date.now();
  const filename = `${name}_${timestamp}.png`;
  const filepath = path.join(screenshotDir, filename);
  
  await page.screenshot({ path: filepath, fullPage: true });
  console.log(`截图已保存: ${filepath}`);
  return filepath;
}

// 主测试函数
async function debugVueComponent() {
  let browser;
  
  try {
    console.log('启动浏览器...');
    browser = await puppeteer.launch({
      headless: false,
      defaultViewport: { width: 1280, height: 720 },
      args: ['--no-sandbox', '--disable-setuid-sandbox']
    });
    
    const page = await browser.newPage();
    
    // 监听控制台消息
    page.on('console', msg => {
      console.log(`[浏览器控制台] ${msg.type()}: ${msg.text()}`);
    });
    
    // 监听页面错误
    page.on('pageerror', error => {
      console.error(`[页面错误] ${error.message}`);
    });
    
    console.log('\n=== 步骤1: 访问首页并登录 ===');
    await page.goto(config.baseUrl, { waitUntil: 'networkidle2' });
    
    // 登录
    await waitForElementAndType(page, '#username', config.studentCredentials.username);
    await waitForElementAndType(page, '#password', config.studentCredentials.password);
    await waitForElementAndClick(page, '.login-btn');
    
    // 等待登录完成
    await new Promise(resolve => setTimeout(resolve, 3000));
    
    // 检查是否需要点击学生账号按钮
    const currentUrl = page.url();
    if (currentUrl === config.baseUrl + '/') {
      console.log('仍在首页，尝试点击学生账号按钮...');
      await waitForElementAndClick(page, '.student-btn');
      await new Promise(resolve => setTimeout(resolve, 2000));
    }
    
    console.log('\n=== 步骤2: 导航到结果页面 ===');
    // 点击查看结果按钮
    const viewResultBtn = await page.$('[data-testid="view-result-btn"]');
    if (viewResultBtn) {
      await viewResultBtn.click();
      await new Promise(resolve => setTimeout(resolve, 3000));
    } else {
      console.log('未找到查看结果按钮，直接导航到结果页面');
      await page.goto(`${config.baseUrl}/submissions/770e8400-e29b-41d4-a716-446655440001/result`);
      await new Promise(resolve => setTimeout(resolve, 3000));
    }
    
    console.log('\n=== 步骤3: 调试Vue组件状态 ===');
    
    // 等待页面完全加载
    await new Promise(resolve => setTimeout(resolve, 5000));
    
    // 获取Vue组件数据
    const vueDebugInfo = await page.evaluate(() => {
      const results = {
        vueVersion: null,
        appInstance: null,
        submissionResult: null,
        questionsData: null,
        componentState: null,
        errors: []
      };
      
      try {
        // 检查Vue版本
        if (window.Vue) {
          results.vueVersion = window.Vue.version || 'Vue detected';
        }
        
        // 尝试获取Vue应用实例
        const app = document.querySelector('#app')?.__vue_app__;
        if (app) {
          results.appInstance = 'Vue app instance found';
        }
        
        // 尝试从DOM中获取组件数据
        const submissionResultDiv = document.querySelector('.submission-result');
        if (submissionResultDiv) {
          // 检查是否有submissionid属性
          const submissionId = submissionResultDiv.getAttribute('submissionid');
          results.submissionResult = {
            hasSubmissionId: !!submissionId,
            submissionId: submissionId
          };
        }
        
        // 检查questions section
        const questionsSection = document.querySelector('.questions-section');
        if (questionsSection) {
          const questionCards = questionsSection.querySelectorAll('.question-card');
          const videoSections = questionsSection.querySelectorAll('.video-section');
          const videoPlayers = questionsSection.querySelectorAll('.video-player');
          
          results.questionsData = {
            questionCards: questionCards.length,
            videoSections: videoSections.length,
            videoPlayers: videoPlayers.length,
            innerHTML: questionsSection.innerHTML.substring(0, 500) // 前500字符
          };
        }
        
        // 检查是否有loading状态
        const loadingElement = document.querySelector('.loading');
        const errorElement = document.querySelector('.error');
        
        results.componentState = {
          hasLoading: !!loadingElement,
          hasError: !!errorElement,
          loadingVisible: loadingElement ? !loadingElement.hidden : false,
          errorVisible: errorElement ? !errorElement.hidden : false
        };
        
        // 检查网络请求状态
        if (window.performance) {
          const entries = performance.getEntriesByType('resource');
          const apiCalls = entries.filter(entry => 
            entry.name.includes('/api/submissions/') && 
            entry.name.includes('/result')
          );
          
          results.apiCalls = apiCalls.map(call => ({
            url: call.name,
            duration: call.duration,
            responseEnd: call.responseEnd
          }));
        }
        
      } catch (error) {
        results.errors.push(error.message);
      }
      
      return results;
    });
    
    console.log('\n=== Vue组件调试信息 ===');
    console.log('Vue版本:', vueDebugInfo.vueVersion);
    console.log('应用实例:', vueDebugInfo.appInstance);
    console.log('提交结果数据:', JSON.stringify(vueDebugInfo.submissionResult, null, 2));
    console.log('问题数据:', JSON.stringify(vueDebugInfo.questionsData, null, 2));
    console.log('组件状态:', JSON.stringify(vueDebugInfo.componentState, null, 2));
    console.log('API调用:', JSON.stringify(vueDebugInfo.apiCalls, null, 2));
    console.log('错误:', vueDebugInfo.errors);
    
    // 检查页面HTML结构
    const pageStructure = await page.evaluate(() => {
      const questionsSection = document.querySelector('.questions-section');
      return {
        hasQuestionsSection: !!questionsSection,
        questionsSectionHTML: questionsSection ? questionsSection.outerHTML : null,
        bodyHTML: document.body.innerHTML.substring(0, 1000) // 前1000字符
      };
    });
    
    console.log('\n=== 页面结构调试 ===');
    console.log('是否有questions-section:', pageStructure.hasQuestionsSection);
    if (pageStructure.questionsSectionHTML) {
      console.log('questions-section HTML:', pageStructure.questionsSectionHTML.substring(0, 500));
    }
    
    // 尝试手动触发数据获取
    console.log('\n=== 步骤4: 手动触发数据刷新 ===');
    await page.evaluate(() => {
      // 尝试触发页面刷新或重新获取数据
      if (window.location.reload) {
        console.log('尝试刷新页面...');
        // 不实际刷新，只是测试
      }
    });
    
    // 等待一段时间看是否有变化
    await new Promise(resolve => setTimeout(resolve, 3000));
    
    // 再次检查组件状态
    const finalCheck = await page.evaluate(() => {
      const questionsSection = document.querySelector('.questions-section');
      const questionCards = document.querySelectorAll('.question-card');
      const videoSections = document.querySelectorAll('.video-section');
      
      return {
        questionCards: questionCards.length,
        videoSections: videoSections.length,
        questionsHTML: questionsSection ? questionsSection.innerHTML : 'No questions section'
      };
    });
    
    console.log('\n=== 最终检查结果 ===');
    console.log('问题卡片数量:', finalCheck.questionCards);
    console.log('视频区域数量:', finalCheck.videoSections);
    console.log('问题区域HTML:', finalCheck.questionsHTML.substring(0, 300));
    
    // 截图
    await takeScreenshot(page, 'vue_component_debug');
    
    // 保存调试报告
    const report = {
      timestamp: new Date().toISOString(),
      vueDebugInfo,
      pageStructure,
      finalCheck,
      currentUrl: page.url()
    };
    
    const reportPath = path.join(__dirname, 'reports', `vue_debug_report_${Date.now()}.json`);
    const reportsDir = path.dirname(reportPath);
    if (!fs.existsSync(reportsDir)) {
      fs.mkdirSync(reportsDir, { recursive: true });
    }
    
    fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));
    console.log(`\n调试报告已保存到: ${reportPath}`);
    
  } catch (error) {
    console.error('调试过程中出现错误:', error);
  } finally {
    if (browser) {
      await browser.close();
    }
  }
}

// 运行调试
debugVueComponent().catch(console.error);