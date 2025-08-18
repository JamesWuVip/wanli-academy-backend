const puppeteer = require('puppeteer');

(async () => {
  const browser = await puppeteer.launch({ headless: false, slowMo: 100 });
  const page = await browser.newPage();
  
  try {
    // 访问首页
    await page.goto('http://localhost:5173');
    
    // 登录
    await page.type('#username', 'test_student1');
    await page.type('#password', 'password123');
    await page.click('.login-btn');
    
    // 等待登录完成
    await page.waitForNavigation();
    
    console.log('✅ 登录成功，当前URL:', page.url());
    
    // 监听控制台消息和网络请求
    page.on('console', msg => {
      console.log(`🚨 控制台${msg.type()}: ${msg.text()}`);
    });

    page.on('response', response => {
      if (response.url().includes('/api/')) {
        console.log('🌐 API请求:', response.status(), response.url());
      }
    });

    // 等待作业列表API调用
    console.log('⏳ 等待作业列表加载...');
    await new Promise(resolve => setTimeout(resolve, 3000));
    
    // 检查页面上所有的按钮
    const buttons = await page.evaluate(() => {
      const allButtons = Array.from(document.querySelectorAll('button'));
      return allButtons.map(btn => ({
        text: btn.textContent?.trim(),
        className: btn.className,
        id: btn.id,
        dataTestId: btn.getAttribute('data-testid'),
        visible: btn.offsetParent !== null
      }));
    });
    
    console.log('\n📋 页面上的所有按钮:');
    buttons.forEach((btn, index) => {
      console.log(`${index + 1}. 文本: "${btn.text}", 类名: "${btn.className}", ID: "${btn.id}", data-testid: "${btn.dataTestId}", 可见: ${btn.visible}`);
    });
    
    // 检查作业卡片
    const assignments = await page.evaluate(() => {
      const cards = Array.from(document.querySelectorAll('.assignment-card'));
      return cards.map((card, index) => {
        const title = card.querySelector('.assignment-title')?.textContent?.trim();
        const status = card.querySelector('.status-badge')?.textContent?.trim();
        const buttons = Array.from(card.querySelectorAll('button')).map(btn => ({
          text: btn.textContent?.trim(),
          className: btn.className,
          dataTestId: btn.getAttribute('data-testid'),
          visible: btn.offsetParent !== null
        }));
        return { index, title, status, buttons };
      });
    });
    
    console.log('\n📋 作业卡片信息:');
    assignments.forEach(assignment => {
      console.log(`作业 ${assignment.index + 1}: ${assignment.title} (状态: ${assignment.status})`);
      assignment.buttons.forEach((btn, btnIndex) => {
        console.log(`  按钮 ${btnIndex + 1}: "${btn.text}" (类名: ${btn.className}, data-testid: ${btn.dataTestId}, 可见: ${btn.visible})`);
      });
    });
    
    // 等待用户查看
    console.log('\n🔍 页面调试完成，10秒后自动关闭浏览器...');
    await new Promise(resolve => setTimeout(resolve, 10000));
    
  } catch (error) {
    console.error('❌ 调试过程中出错:', error);
  } finally {
    await browser.close();
  }
})();