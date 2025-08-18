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

// 测试结果记录
const testResults = {
  totalTests: 0,
  passedTests: 0,
  failedTests: 0,
  testDetails: [],
  startTime: new Date(),
  endTime: null
};

// 辅助函数：记录测试结果
function recordTest(testName, passed, details = '', screenshot = null) {
  testResults.totalTests++;
  if (passed) {
    testResults.passedTests++;
  } else {
    testResults.failedTests++;
  }
  
  testResults.testDetails.push({
    name: testName,
    passed,
    details,
    screenshot,
    timestamp: new Date()
  });
  
  console.log(`${passed ? '✅' : '❌'} ${testName}: ${details}`);
}

// 辅助函数：等待元素出现
async function waitForElement(page, selector, timeout = 10000) {
  try {
    await page.waitForSelector(selector, { timeout });
    return true;
  } catch (error) {
    return false;
  }
}

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
async function runBrowserAcceptanceTest() {
  let browser;
  let page;
  
  try {
    console.log('🚀 启动Sprint 4浏览器自动化验收测试...');
    
    // 启动浏览器（非headless模式，便于调试）
    browser = await puppeteer.launch({
      headless: false,
      slowMo: 50, // 减慢操作速度，便于观察
      args: ['--no-sandbox', '--disable-setuid-sandbox']
    });
    
    page = await browser.newPage();
    await page.setViewport({ width: 1280, height: 720 });
    
    // 测试1: 访问首页
    console.log('\n📋 测试1: 访问应用首页');
    try {
      await page.goto(config.baseUrl, { waitUntil: 'networkidle2' });
      const title = await page.title();
      const screenshot = await takeScreenshot(page, 'homepage');
      recordTest('访问应用首页', true, `页面标题: ${title}`, screenshot);
    } catch (error) {
      const screenshot = await takeScreenshot(page, 'homepage_error');
      recordTest('访问应用首页', false, `错误: ${error.message}`, screenshot);
      throw error;
    }
    
    // 测试2: 学员登录
    console.log('\n📋 测试2: 学员身份登录');
    try {
      // 登录表单在首页，无需导航
      // 等待页面完全加载
      await new Promise(resolve => setTimeout(resolve, 2000));
      
      // 等待登录表单加载
      await page.waitForSelector('#username', { timeout: 15000 });
      await page.waitForSelector('#password', { timeout: 15000 });
      await page.waitForSelector('.login-btn', { timeout: 15000 });
      
      // 清空并输入学员凭据
      await page.click('#username');
      await page.keyboard.down('Control');
      await page.keyboard.press('KeyA');
      await page.keyboard.up('Control');
      await page.type('#username', config.studentCredentials.username);
      
      await page.click('#password');
      await page.keyboard.down('Control');
      await page.keyboard.press('KeyA');
      await page.keyboard.up('Control');
      await page.type('#password', config.studentCredentials.password);
      
      const screenshot1 = await takeScreenshot(page, 'login_form_filled');
      
      // 点击登录按钮
      await page.click('.login-btn');
      
      // 等待登录完成（可能跳转到作业列表或仪表板）
      await page.waitForNavigation({ waitUntil: 'networkidle2', timeout: 15000 });
      
      const currentUrl = page.url();
      const screenshot2 = await takeScreenshot(page, 'after_login');
      
      recordTest('学员身份登录', true, `登录成功，当前URL: ${currentUrl}`, screenshot2);
    } catch (error) {
      const screenshot = await takeScreenshot(page, 'login_error');
      recordTest('学员身份登录', false, `登录失败: ${error.message}`, screenshot);
      throw error;
    }
    
    // 测试3: 查看作业列表
    console.log('\n📋 测试3: 验证作业列表页面');
    try {
      // 导航到作业列表（如果不在的话）
      const currentUrl = page.url();
      if (!currentUrl.includes('/assignments') && !currentUrl.includes('/homework')) {
        // 查找作业列表链接
        const assignmentLinkExists = await waitForElement(page, 'a[href*="assignment"], a[href*="homework"], .nav-assignments');
        if (assignmentLinkExists) {
          await page.click('a[href*="assignment"], a[href*="homework"], .nav-assignments');
          await page.waitForNavigation({ waitUntil: 'networkidle2' });
        }
      }
      
      // 等待作业列表加载
      await page.waitForSelector('.assignment-list, .homework-list, .assignment-item, .homework-item', { timeout: 15000 });
      
      // 检查是否有作业项目
      const assignmentItems = await page.$$('.assignment-item, .homework-item, .assignment-card');
      const screenshot = await takeScreenshot(page, 'assignment_list');
      
      recordTest('验证作业列表页面', assignmentItems.length > 0, 
        `找到 ${assignmentItems.length} 个作业项目`, screenshot);
    } catch (error) {
      const screenshot = await takeScreenshot(page, 'assignment_list_error');
      recordTest('验证作业列表页面', false, `无法加载作业列表: ${error.message}`, screenshot);
      throw error;
    }
    
    // 测试4: 查找并点击"查看结果"链接
    console.log('\n📋 测试4: 查找已批改作业并点击查看结果');
    try {
      // 查找"查看结果"按钮或链接
      const viewResultSelectors = [
        'a:contains("查看结果")',
        'button:contains("查看结果")',
        '.view-result',
        '.result-btn',
        'a[href*="result"]',
        'button[data-action="view-result"]'
      ];
      
      let resultButtonFound = false;
      let resultButton = null;
      
      for (const selector of viewResultSelectors) {
        try {
          resultButton = await page.$(selector);
          if (resultButton) {
            resultButtonFound = true;
            break;
          }
        } catch (e) {
          // 继续尝试下一个选择器
        }
      }
      
      if (!resultButtonFound) {
        // 尝试通过文本内容查找
        const elements = await page.$$('a, button');
        for (const element of elements) {
          const text = await page.evaluate(el => el.textContent, element);
          if (text && (text.includes('查看结果') || text.includes('结果') || text.includes('查看'))) {
            resultButton = element;
            resultButtonFound = true;
            break;
          }
        }
      }
      
      const screenshot1 = await takeScreenshot(page, 'before_click_result');
      
      if (resultButtonFound && resultButton) {
        await resultButton.click();
        await page.waitForNavigation({ waitUntil: 'networkidle2', timeout: 15000 });
        
        const currentUrl = page.url();
        const screenshot2 = await takeScreenshot(page, 'after_click_result');
        
        recordTest('点击查看结果链接', true, `成功跳转到结果页面: ${currentUrl}`, screenshot2);
      } else {
        recordTest('点击查看结果链接', false, '未找到"查看结果"按钮或链接', screenshot1);
        throw new Error('未找到查看结果按钮');
      }
    } catch (error) {
      const screenshot = await takeScreenshot(page, 'click_result_error');
      recordTest('点击查看结果链接', false, `点击查看结果失败: ${error.message}`, screenshot);
      throw error;
    }
    
    // 测试5: 验证结果页面内容 - 总分和教师评语
    console.log('\n📋 测试5: 验证结果页面显示总分和教师评语');
    try {
      // 等待结果页面加载
      await page.waitForSelector('.result-container, .submission-result, .score-section', { timeout: 15000 });
      
      // 查找总分
      const scoreSelectors = ['.score', '.total-score', '.final-score', '[data-testid="score"]'];
      let scoreFound = false;
      let scoreText = '';
      
      for (const selector of scoreSelectors) {
        try {
          const scoreElement = await page.$(selector);
          if (scoreElement) {
            scoreText = await page.evaluate(el => el.textContent, scoreElement);
            if (scoreText && (scoreText.includes('分') || /\d+/.test(scoreText))) {
              scoreFound = true;
              break;
            }
          }
        } catch (e) {
          // 继续尝试
        }
      }
      
      // 查找教师评语
      const feedbackSelectors = ['.teacher-feedback', '.feedback', '.comment', '.teacher-comment', '[data-testid="feedback"]'];
      let feedbackFound = false;
      let feedbackText = '';
      
      for (const selector of feedbackSelectors) {
        try {
          const feedbackElement = await page.$(selector);
          if (feedbackElement) {
            feedbackText = await page.evaluate(el => el.textContent, feedbackElement);
            if (feedbackText && feedbackText.trim().length > 0) {
              feedbackFound = true;
              break;
            }
          }
        } catch (e) {
          // 继续尝试
        }
      }
      
      const screenshot = await takeScreenshot(page, 'result_page_content');
      
      const bothFound = scoreFound && feedbackFound;
      recordTest('验证总分和教师评语显示', bothFound, 
        `总分: ${scoreFound ? scoreText : '未找到'}, 教师评语: ${feedbackFound ? '已找到' : '未找到'}`, screenshot);
    } catch (error) {
      const screenshot = await takeScreenshot(page, 'result_content_error');
      recordTest('验证总分和教师评语显示', false, `验证结果页面内容失败: ${error.message}`, screenshot);
    }
    
    // 测试6: 验证答案对比功能
    console.log('\n📋 测试6: 验证逐题答案对比功能');
    try {
      // 查找题目和答案对比部分
      const questionSelectors = ['.question-item', '.question', '.answer-comparison', '.question-result'];
      let questionsFound = false;
      let questionCount = 0;
      
      for (const selector of questionSelectors) {
        try {
          const questionElements = await page.$$(selector);
          if (questionElements && questionElements.length > 0) {
            questionCount = questionElements.length;
            questionsFound = true;
            break;
          }
        } catch (e) {
          // 继续尝试
        }
      }
      
      // 查找学生答案和标准答案
      const studentAnswerSelectors = ['.student-answer', '.my-answer', '.user-answer'];
      const standardAnswerSelectors = ['.standard-answer', '.correct-answer', '.reference-answer'];
      
      let studentAnswerFound = false;
      let standardAnswerFound = false;
      
      for (const selector of studentAnswerSelectors) {
        const element = await page.$(selector);
        if (element) {
          studentAnswerFound = true;
          break;
        }
      }
      
      for (const selector of standardAnswerSelectors) {
        const element = await page.$(selector);
        if (element) {
          standardAnswerFound = true;
          break;
        }
      }
      
      const screenshot = await takeScreenshot(page, 'answer_comparison');
      
      const comparisonComplete = questionsFound && studentAnswerFound && standardAnswerFound;
      recordTest('验证逐题答案对比功能', comparisonComplete, 
        `题目数量: ${questionCount}, 学生答案: ${studentAnswerFound ? '已显示' : '未找到'}, 标准答案: ${standardAnswerFound ? '已显示' : '未找到'}`, screenshot);
    } catch (error) {
      const screenshot = await takeScreenshot(page, 'answer_comparison_error');
      recordTest('验证逐题答案对比功能', false, `验证答案对比失败: ${error.message}`, screenshot);
    }
    
    // 测试7: 验证视频播放功能
    console.log('\n📋 测试7: 验证教师讲解视频播放功能');
    try {
      // 查找视频元素
      const videoSelectors = ['video', '.video-player', '.explanation-video', '[data-testid="video"]'];
      let videoFound = false;
      let videoElement = null;
      
      for (const selector of videoSelectors) {
        try {
          videoElement = await page.$(selector);
          if (videoElement) {
            videoFound = true;
            break;
          }
        } catch (e) {
          // 继续尝试
        }
      }
      
      // 查找视频播放按钮
      const playButtonSelectors = ['.play-btn', '.video-play', 'button[aria-label*="play"]', '.play-button'];
      let playButtonFound = false;
      
      for (const selector of playButtonSelectors) {
        try {
          const playButton = await page.$(selector);
          if (playButton) {
            playButtonFound = true;
            // 尝试点击播放按钮
            await playButton.click();
            await page.waitForTimeout(2000); // 等待视频开始播放
            break;
          }
        } catch (e) {
          // 继续尝试
        }
      }
      
      const screenshot = await takeScreenshot(page, 'video_playback');
      
      const videoPlaybackAvailable = videoFound || playButtonFound;
      recordTest('验证教师讲解视频播放功能', videoPlaybackAvailable, 
        `视频元素: ${videoFound ? '已找到' : '未找到'}, 播放按钮: ${playButtonFound ? '已找到并点击' : '未找到'}`, screenshot);
    } catch (error) {
      const screenshot = await takeScreenshot(page, 'video_playback_error');
      recordTest('验证教师讲解视频播放功能', false, `验证视频播放失败: ${error.message}`, screenshot);
    }
    
  } catch (error) {
    console.error('❌ 测试过程中发生严重错误:', error);
    if (page) {
      await takeScreenshot(page, 'critical_error');
    }
  } finally {
    // 关闭浏览器
    if (browser) {
      await browser.close();
    }
    
    // 记录测试结束时间
    testResults.endTime = new Date();
    
    // 生成测试报告
    await generateTestReport();
  }
}

// 生成测试报告
async function generateTestReport() {
  console.log('\n📊 生成测试报告...');
  
  const duration = testResults.endTime - testResults.startTime;
  const successRate = ((testResults.passedTests / testResults.totalTests) * 100).toFixed(2);
  
  const report = {
    title: 'Sprint 4 浏览器自动化验收测试报告',
    summary: {
      totalTests: testResults.totalTests,
      passedTests: testResults.passedTests,
      failedTests: testResults.failedTests,
      successRate: `${successRate}%`,
      duration: `${Math.round(duration / 1000)}秒`,
      startTime: testResults.startTime.toISOString(),
      endTime: testResults.endTime.toISOString()
    },
    testDetails: testResults.testDetails,
    conclusion: successRate >= 85 ? '✅ 验收测试通过' : '❌ 验收测试未通过'
  };
  
  // 保存JSON报告
  const jsonReportPath = path.join(__dirname, 'reports', 'sprint4_browser_acceptance_report.json');
  const reportsDir = path.dirname(jsonReportPath);
  if (!fs.existsSync(reportsDir)) {
    fs.mkdirSync(reportsDir, { recursive: true });
  }
  
  fs.writeFileSync(jsonReportPath, JSON.stringify(report, null, 2));
  
  // 生成Markdown报告
  const markdownReport = generateMarkdownReport(report);
  const mdReportPath = path.join(__dirname, 'reports', 'Sprint4_Browser_Acceptance_Test_Report.md');
  fs.writeFileSync(mdReportPath, markdownReport);
  
  // 控制台输出摘要
  console.log('\n' + '='.repeat(60));
  console.log('📋 Sprint 4 浏览器自动化验收测试报告');
  console.log('='.repeat(60));
  console.log(`总测试数: ${report.summary.totalTests}`);
  console.log(`通过测试: ${report.summary.passedTests}`);
  console.log(`失败测试: ${report.summary.failedTests}`);
  console.log(`成功率: ${report.summary.successRate}`);
  console.log(`测试时长: ${report.summary.duration}`);
  console.log(`结论: ${report.conclusion}`);
  console.log('='.repeat(60));
  
  console.log(`\n📄 详细报告已保存到:`);
  console.log(`- JSON: ${jsonReportPath}`);
  console.log(`- Markdown: ${mdReportPath}`);
}

// 生成Markdown报告
function generateMarkdownReport(report) {
  let markdown = `# ${report.title}\n\n`;
  
  markdown += `## 测试摘要\n\n`;
  markdown += `| 指标 | 值 |\n`;
  markdown += `|------|-----|\n`;
  markdown += `| 总测试数 | ${report.summary.totalTests} |\n`;
  markdown += `| 通过测试 | ${report.summary.passedTests} |\n`;
  markdown += `| 失败测试 | ${report.summary.failedTests} |\n`;
  markdown += `| 成功率 | ${report.summary.successRate} |\n`;
  markdown += `| 测试时长 | ${report.summary.duration} |\n`;
  markdown += `| 开始时间 | ${report.summary.startTime} |\n`;
  markdown += `| 结束时间 | ${report.summary.endTime} |\n\n`;
  
  markdown += `## 测试结果详情\n\n`;
  
  report.testDetails.forEach((test, index) => {
    const status = test.passed ? '✅ 通过' : '❌ 失败';
    markdown += `### ${index + 1}. ${test.name}\n\n`;
    markdown += `**状态**: ${status}\n\n`;
    markdown += `**详情**: ${test.details}\n\n`;
    markdown += `**时间**: ${test.timestamp.toISOString()}\n\n`;
    if (test.screenshot) {
      markdown += `**截图**: ${test.screenshot}\n\n`;
    }
    markdown += `---\n\n`;
  });
  
  markdown += `## 结论\n\n`;
  markdown += `${report.conclusion}\n\n`;
  
  if (report.summary.successRate >= 85) {
    markdown += `🎉 Sprint 4的核心验收标准已通过浏览器自动化测试验证！\n\n`;
    markdown += `### 验证的功能点:\n`;
    markdown += `- ✅ 学员身份登录\n`;
    markdown += `- ✅ 作业列表查看\n`;
    markdown += `- ✅ 查看结果功能\n`;
    markdown += `- ✅ 总分和教师评语显示\n`;
    markdown += `- ✅ 答案对比功能\n`;
    markdown += `- ✅ 视频播放功能\n\n`;
  } else {
    markdown += `⚠️ 部分功能需要进一步完善，请查看失败的测试项目。\n\n`;
  }
  
  return markdown;
}

// 运行测试
if (require.main === module) {
  runBrowserAcceptanceTest().catch(console.error);
}

module.exports = { runBrowserAcceptanceTest };