const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');

// 测试配置
const config = {
  frontendURL: 'http://localhost:5173',
  studentCredentials: {
    username: 'test_student1',
    password: 'password123'
  },
  timeout: 30000,
  screenshotDir: path.join(__dirname, 'screenshots'),
  reportsDir: path.join(__dirname, 'reports')
};

// 测试结果存储
let testResults = [];
let testStartTime = Date.now();
let browser = null;
let page = null;

// 辅助函数：记录测试结果
function recordTest(testName, passed, details = '', error = null, screenshotPath = null) {
  const result = {
    testName,
    passed,
    details,
    error: error ? error.message : null,
    screenshotPath,
    timestamp: new Date().toISOString()
  };
  testResults.push(result);
  
  const status = passed ? '✅' : '❌';
  console.log(`${status} ${testName}: ${details}`);
  if (error) {
    console.log(`   错误: ${error.message}`);
  }
  if (screenshotPath) {
    console.log(`   截图: ${screenshotPath}`);
  }
}

// 辅助函数：截图
async function takeScreenshot(name, description = '') {
  try {
    if (!fs.existsSync(config.screenshotDir)) {
      fs.mkdirSync(config.screenshotDir, { recursive: true });
    }
    
    const timestamp = Date.now();
    const filename = `${name}_${timestamp}.png`;
    const screenshotPath = path.join(config.screenshotDir, filename);
    
    await page.screenshot({ 
      path: screenshotPath, 
      fullPage: true,
      type: 'png'
    });
    
    console.log(`📸 截图已保存: ${filename} - ${description}`);
    return screenshotPath;
  } catch (error) {
    console.error('截图失败:', error.message);
    return null;
  }
}

// 辅助函数：等待元素并点击
async function waitAndClick(selector, description, timeout = 15000) {
  try {
    console.log(`🔍 等待元素: ${selector} (${description})`);
    await page.waitForSelector(selector, { timeout });
    
    // 确保元素可见
    await page.waitForFunction(
      (sel) => {
        const element = document.querySelector(sel);
        return element && element.offsetParent !== null;
      },
      { timeout: 5000 },
      selector
    );
    
    await page.click(selector);
    console.log(`✅ 成功点击: ${description}`);
    return true;
  } catch (error) {
    console.error(`❌ 点击失败 ${description}:`, error.message);
    return false;
  }
}

// 辅助函数：等待元素并输入文本
async function waitAndType(selector, text, description, timeout = 15000) {
  try {
    console.log(`🔍 等待输入框: ${selector} (${description})`);
    await page.waitForSelector(selector, { timeout });
    
    // 清空输入框并输入文本
    await page.click(selector);
    await page.evaluate((sel) => {
      document.querySelector(sel).value = '';
    }, selector);
    await page.type(selector, text, { delay: 50 });
    
    console.log(`✅ 成功输入: ${description}`);
    return true;
  } catch (error) {
    console.error(`❌ 输入失败 ${description}:`, error.message);
    return false;
  }
}

// 测试1: 启动浏览器并访问首页
async function testBrowserLaunch() {
  console.log('\n📋 测试1: 启动浏览器并访问首页');
  
  try {
    browser = await puppeteer.launch({
      headless: false, // 显示浏览器界面
      slowMo: 100,     // 减慢操作速度
      defaultViewport: { width: 1280, height: 800 },
      args: [
        '--no-sandbox',
        '--disable-setuid-sandbox',
        '--disable-dev-shm-usage',
        '--disable-web-security'
      ]
    });
    
    page = await browser.newPage();
    
    // 设置用户代理
    await page.setUserAgent('Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36');
    
    console.log(`🌐 访问前端应用: ${config.frontendURL}`);
    await page.goto(config.frontendURL, { 
      waitUntil: 'networkidle2',
      timeout: config.timeout 
    });
    
    // 等待页面完全加载
     await new Promise(resolve => setTimeout(resolve, 3000));
    
    const title = await page.title();
    const url = page.url();
    
    const screenshotPath = await takeScreenshot('homepage', '首页加载完成');
    
    recordTest('启动浏览器并访问首页', true, `页面标题: ${title}, URL: ${url}`, null, screenshotPath);
    return true;
    
  } catch (error) {
    const screenshotPath = await takeScreenshot('homepage_error', '首页访问失败');
    recordTest('启动浏览器并访问首页', false, '无法访问前端应用', error, screenshotPath);
    return false;
  }
}

// 测试2: 用户登录
async function testUserLogin() {
  console.log('\n📋 测试2: 用户登录');
  
  try {
    // 查找并填写用户名
    const usernameSuccess = await waitAndType(
      '#username', 
      config.studentCredentials.username, 
      '用户名输入框'
    );
    
    if (!usernameSuccess) {
      throw new Error('无法找到或填写用户名输入框');
    }
    
    // 查找并填写密码
    const passwordSuccess = await waitAndType(
      '#password', 
      config.studentCredentials.password, 
      '密码输入框'
    );
    
    if (!passwordSuccess) {
      throw new Error('无法找到或填写密码输入框');
    }
    
    // 截图：登录表单填写完成
    const loginFormScreenshot = await takeScreenshot('login_form_filled', '登录表单填写完成');
    
    // 点击登录按钮
    const loginSuccess = await waitAndClick('.login-btn', '登录按钮');
    
    if (!loginSuccess) {
      throw new Error('无法找到或点击登录按钮');
    }
    
    // 等待登录完成，检查URL变化或页面内容变化
     console.log('⏳ 等待登录完成...');
     await new Promise(resolve => setTimeout(resolve, 3000));
    
    // 检查是否登录成功（URL变化或页面内容变化）
    const currentUrl = page.url();
    const pageContent = await page.content();
    
    // 检查是否包含登录后的内容
    const isLoggedIn = pageContent.includes('作业列表') || 
                      pageContent.includes('我的作业') || 
                      currentUrl.includes('/assignments') ||
                      currentUrl.includes('/dashboard');
    
    const screenshotPath = await takeScreenshot('after_login', '登录后页面状态');
    
    if (isLoggedIn) {
      recordTest('用户登录', true, `登录成功，当前URL: ${currentUrl}`, null, screenshotPath);
      return true;
    } else {
      throw new Error('登录失败，未检测到登录后的页面内容');
    }
    
  } catch (error) {
    const screenshotPath = await takeScreenshot('login_error', '登录失败');
    recordTest('用户登录', false, '登录过程中出现错误', error, screenshotPath);
    return false;
  }
}

// 测试3: 导航到作业列表
async function testNavigateToAssignments() {
  console.log('\n📋 测试3: 导航到作业列表');
  
  try {
    // 等待并点击作业列表链接或按钮
    const navigationSuccess = await waitAndClick('[data-testid="assignments-link"], a[href*="assignments"], .nav-link[href*="assignments"], .nav-link', '作业列表导航');
    
    if (!navigationSuccess) {
      // 尝试其他可能的选择器
      const altNavigationSuccess = await waitAndClick('.assignments-link, nav a:contains("作业"), .menu-item:contains("作业")', '作业列表导航(备选)');
      if (!altNavigationSuccess) {
        throw new Error('无法找到作业列表导航链接');
      }
    }
    
    // 等待作业列表页面加载
    await new Promise(resolve => setTimeout(resolve, 2000));
    
    const currentUrl = page.url();
    const pageContent = await page.content();
    
    // 检查是否成功导航到作业列表页面
    const isOnAssignmentsPage = pageContent.includes('作业列表') || 
                               pageContent.includes('我的作业') ||
                               currentUrl.includes('/assignments');
    
    const screenshotPath = await takeScreenshot('assignments_page', '作业列表页面');
    
    if (isOnAssignmentsPage) {
      recordTest('导航到作业列表', true, `成功访问作业列表页面，URL: ${currentUrl}`, null, screenshotPath);
      return true;
    } else {
      throw new Error('未能成功导航到作业列表页面');
    }
    
  } catch (error) {
    const screenshotPath = await takeScreenshot('navigation_error', '导航失败');
    recordTest('导航到作业列表', false, '导航到作业列表失败', error, screenshotPath);
    return false;
  }
}

// 测试4: 点击查看结果
async function testViewResult() {
  console.log('\n📋 测试4: 点击查看结果');
  
  try {
    // 点击查看结果按钮 - 寻找第一个可用的查看结果按钮
    console.log('尝试点击查看结果按钮');
    const viewResultSelectors = [
      '[data-testid="view-result-btn"]',
      '.view-result-btn',
      'button:contains("📊 查看结果")',
      'button[class*="view-result"]',
      'text=查看结果',
      '.action-btn'
    ];
    
    let viewResultFound = false;
    // 等待页面加载完成
    await new Promise(resolve => setTimeout(resolve, 2000));
    
    for (const selector of viewResultSelectors) {
      try {
        // 检查是否存在该选择器的元素
        const elements = await page.$$(selector);
        if (elements.length > 0) {
          // 点击第一个找到的查看结果按钮
          await elements[0].click();
          viewResultFound = true;
          console.log(`✅ 成功点击查看结果按钮: ${selector}`);
          break;
        }
      } catch (error) {
        console.log(`选择器 ${selector} 未找到，尝试下一个`);
      }
    }
    
    if (!viewResultFound) {
      throw new Error('无法找到或点击查看结果按钮');
    }
    
    // 等待结果页面加载
    await new Promise(resolve => setTimeout(resolve, 3000));
    
    const currentUrl = page.url();
    const screenshotPath = await takeScreenshot('result_page', '作业结果页面');
    
    recordTest('点击查看结果', true, `成功进入结果页面，URL: ${currentUrl}`, null, screenshotPath);
    return true;
    
  } catch (error) {
    const screenshotPath = await takeScreenshot('view_result_error', '查看结果失败');
    recordTest('点击查看结果', false, '点击查看结果失败', error, screenshotPath);
    return false;
  }
}

// 测试5: 验证页面内容
async function testVerifyPageContent() {
  console.log('\n📋 测试5: 验证页面内容');
  
  try {
    const pageContent = await page.content();
    const verificationResults = [];
    
    // 检查总分显示
    const hasScore = pageContent.includes('总分') || pageContent.includes('得分') || pageContent.includes('分数');
    verificationResults.push({ item: '总分显示', passed: hasScore });
    
    // 检查教师评语
    const hasTeacherComment = pageContent.includes('教师评语') || pageContent.includes('老师评语') || pageContent.includes('评语');
    verificationResults.push({ item: '教师评语', passed: hasTeacherComment });
    
    // 检查答案对比
    const hasAnswerComparison = pageContent.includes('答案对比') || pageContent.includes('正确答案') || pageContent.includes('我的答案');
    verificationResults.push({ item: '答案对比', passed: hasAnswerComparison });
    
    // 检查视频播放器
    const hasVideoPlayer = pageContent.includes('video') || pageContent.includes('播放') || pageContent.includes('视频');
    verificationResults.push({ item: '视频播放器', passed: hasVideoPlayer });
    
    const screenshotPath = await takeScreenshot('content_verification', '页面内容验证');
    
    const allPassed = verificationResults.every(result => result.passed);
    const details = verificationResults.map(result => `${result.item}: ${result.passed ? '✅' : '❌'}`).join(', ');
    
    recordTest('验证页面内容', allPassed, details, null, screenshotPath);
    return allPassed;
    
  } catch (error) {
    const screenshotPath = await takeScreenshot('verification_error', '内容验证失败');
    recordTest('验证页面内容', false, '页面内容验证过程中出现错误', error, screenshotPath);
    return false;
  }
}

// 生成测试报告
async function generateReport() {
  console.log('\n📊 生成测试报告');
  
  try {
    if (!fs.existsSync(config.reportsDir)) {
      fs.mkdirSync(config.reportsDir, { recursive: true });
    }
    
    const testEndTime = Date.now();
    const totalDuration = testEndTime - testStartTime;
    const passedTests = testResults.filter(test => test.passed).length;
    const totalTests = testResults.length;
    const successRate = totalTests > 0 ? ((passedTests / totalTests) * 100).toFixed(2) : 0;
    
    const report = {
      summary: {
        totalTests,
        passedTests,
        failedTests: totalTests - passedTests,
        successRate: `${successRate}%`,
        duration: `${(totalDuration / 1000).toFixed(2)}秒`,
        timestamp: new Date().toISOString()
      },
      testResults,
      conclusion: passedTests === totalTests ? 'Sprint 4 浏览器验收测试通过' : 'Sprint 4 浏览器验收测试未通过'
    };
    
    // 保存JSON报告
    const jsonReportPath = path.join(config.reportsDir, `sprint4_browser_test_report_${Date.now()}.json`);
    fs.writeFileSync(jsonReportPath, JSON.stringify(report, null, 2), 'utf8');
    
    // 生成Markdown报告
    const markdownReport = `# Sprint 4 浏览器验收测试报告\n\n## 测试概要\n- **总测试数**: ${totalTests}\n- **通过测试**: ${passedTests}\n- **失败测试**: ${totalTests - passedTests}\n- **成功率**: ${successRate}%\n- **测试时长**: ${(totalDuration / 1000).toFixed(2)}秒\n- **测试时间**: ${new Date().toLocaleString('zh-CN')}\n\n## 测试结果详情\n\n${testResults.map(test => `### ${test.testName}\n- **状态**: ${test.passed ? '✅ 通过' : '❌ 失败'}\n- **详情**: ${test.details}\n${test.error ? `- **错误**: ${test.error}` : ''}\n${test.screenshotPath ? `- **截图**: ${path.basename(test.screenshotPath)}` : ''}\n- **时间**: ${new Date(test.timestamp).toLocaleString('zh-CN')}\n`).join('\n')}\n\n## 结论\n${report.conclusion}\n`;
    
    const markdownReportPath = path.join(config.reportsDir, `sprint4_browser_test_report_${Date.now()}.md`);
    fs.writeFileSync(markdownReportPath, markdownReport, 'utf8');
    
    console.log(`\n📋 测试报告已生成:`);
    console.log(`   JSON: ${jsonReportPath}`);
    console.log(`   Markdown: ${markdownReportPath}`);
    
    return report;
    
  } catch (error) {
    console.error('生成报告失败:', error.message);
    return null;
  }
}

// 主测试函数
async function runBrowserTests() {
  console.log('🚀 开始 Sprint 4 浏览器验收测试');
  console.log(`📅 测试开始时间: ${new Date().toLocaleString('zh-CN')}`);
  
  try {
    // 执行所有测试
    const test1Result = await testBrowserLaunch();
    if (!test1Result) {
      console.log('❌ 浏览器启动失败，终止测试');
      return;
    }
    
    const test2Result = await testUserLogin();
    if (!test2Result) {
      console.log('❌ 用户登录失败，终止测试');
      return;
    }
    
    const test3Result = await testNavigateToAssignments();
    if (!test3Result) {
      console.log('❌ 导航到作业列表失败，继续其他测试');
    }
    
    const test4Result = await testViewResult();
    if (!test4Result) {
      console.log('❌ 查看结果失败，继续其他测试');
    }
    
    const test5Result = await testVerifyPageContent();
    
    // 生成测试报告
    const report = await generateReport();
    
    // 输出测试总结
    console.log('\n🎯 测试总结:');
    if (report) {
      console.log(`   总测试数: ${report.summary.totalTests}`);
      console.log(`   通过测试: ${report.summary.passedTests}`);
      console.log(`   失败测试: ${report.summary.failedTests}`);
      console.log(`   成功率: ${report.summary.successRate}`);
      console.log(`   测试时长: ${report.summary.duration}`);
      console.log(`   结论: ${report.conclusion}`);
    }
    
  } catch (error) {
    console.error('测试执行过程中出现严重错误:', error.message);
  } finally {
    // 清理资源
    if (browser) {
      console.log('🔄 关闭浏览器...');
      await browser.close();
    }
    console.log('✅ 测试完成');
  }
}

// 启动测试
if (require.main === module) {
  runBrowserTests().catch(console.error);
}

module.exports = {
  runBrowserTests,
  config
};