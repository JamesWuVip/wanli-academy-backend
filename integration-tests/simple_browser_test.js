const puppeteer = require('puppeteer');

async function simpleBrowserTest() {
  let browser;
  try {
    console.log('🚀 启动简化浏览器测试...');
    
    browser = await puppeteer.launch({
      headless: false,
      slowMo: 1000,
      args: ['--no-sandbox', '--disable-setuid-sandbox']
    });
    
    const page = await browser.newPage();
    await page.setViewport({ width: 1280, height: 720 });
    
    console.log('📱 访问前端应用...');
    await page.goto('http://localhost:5173', { waitUntil: 'networkidle2' });
    
    // 等待页面加载
    await new Promise(resolve => setTimeout(resolve, 3000));
    
    console.log('🔍 查找快速登录按钮...');
    
    // 尝试找到快速登录的学生账号按钮
    const quickLoginButtons = await page.$$('button');
    console.log(`找到 ${quickLoginButtons.length} 个按钮`);
    
    // 获取所有按钮的文本
    for (let i = 0; i < quickLoginButtons.length; i++) {
      const buttonText = await page.evaluate(el => el.textContent.trim(), quickLoginButtons[i]);
      console.log(`按钮 ${i + 1}: "${buttonText}"`);
      
      if (buttonText.includes('学生账号') || buttonText.includes('学生')) {
        console.log('🎯 找到学生账号快速登录按钮，点击...');
        await quickLoginButtons[i].click();
        
        // 等待登录完成
        await new Promise(resolve => setTimeout(resolve, 3000));
        
        const currentUrl = page.url();
        console.log(`登录后URL: ${currentUrl}`);
        
        if (currentUrl.includes('/assignments') || currentUrl !== 'http://localhost:5173/') {
          console.log('✅ 登录成功！');
          
          // 检查作业列表页面
          console.log('🔍 检查作业列表页面...');
          const pageContent = await page.evaluate(() => document.body.innerText);
          console.log('页面内容:');
          console.log(pageContent.substring(0, 800));
          
          // 查找"查看结果"按钮或链接
          console.log('🔍 查找"查看结果"按钮...');
          const viewResultButtons = await page.$$eval('*', elements => {
            return elements.filter(el => {
              const text = el.textContent || '';
              return text.includes('查看结果') || text.includes('查看') || text.includes('结果');
            }).map(el => ({
              tagName: el.tagName,
              textContent: el.textContent.trim(),
              className: el.className
            }));
          });
          
          console.log('找到的相关元素:');
          viewResultButtons.forEach((btn, index) => {
            console.log(`  ${index + 1}. ${btn.tagName}: "${btn.textContent}" (class: ${btn.className})`);
          });
          
          if (viewResultButtons.length > 0) {
            console.log('✅ 找到查看结果相关元素');
          } else {
            console.log('❌ 未找到查看结果相关元素');
          }
          
        } else {
          console.log('❌ 登录可能失败，URL未改变');
        }
        
        break;
      }
    }
    
    // 等待用户观察
    console.log('\n🎯 测试完成，浏览器将保持打开状态供观察...');
    console.log('按任意键关闭浏览器...');
    
    await new Promise(resolve => {
      process.stdin.once('data', () => resolve());
    });
    
  } catch (error) {
    console.error('❌ 测试过程中发生错误:', error);
  } finally {
    if (browser) {
      await browser.close();
    }
  }
}

// 运行简化测试
simpleBrowserTest().catch(console.error);