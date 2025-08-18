const puppeteer = require('puppeteer');

async function debugBrowserTest() {
  let browser;
  try {
    console.log('🚀 启动浏览器调试测试...');
    
    browser = await puppeteer.launch({
      headless: false, // 显示浏览器窗口
      slowMo: 1000,    // 每个操作延迟1秒
      devtools: true   // 打开开发者工具
    });
    
    const page = await browser.newPage();
    await page.setViewport({ width: 1280, height: 720 });
    
    console.log('📱 访问前端应用...');
    await page.goto('http://localhost:5173', { waitUntil: 'networkidle2' });
    
    // 等待页面加载
    await new Promise(resolve => setTimeout(resolve, 3000));
    
    console.log('📄 获取页面标题和URL...');
    const title = await page.title();
    const url = page.url();
    console.log(`页面标题: ${title}`);
    console.log(`当前URL: ${url}`);
    
    console.log('🔍 检查页面内容...');
    const bodyText = await page.evaluate(() => document.body.innerText);
    console.log('页面文本内容:');
    console.log(bodyText.substring(0, 500));
    
    console.log('🔍 检查是否存在登录表单元素...');
    
    // 检查各种可能的登录元素
    const elements = {
      '#username': await page.$('#username'),
      '#password': await page.$('#password'),
      '.login-btn': await page.$('.login-btn'),
      'input[type="text"]': await page.$('input[type="text"]'),
      'input[type="password"]': await page.$('input[type="password"]'),
      'button': await page.$$('button')
    };
    
    for (const [selector, element] of Object.entries(elements)) {
      if (element) {
        console.log(`✅ 找到元素: ${selector}`);
        if (selector === 'button') {
          console.log(`   按钮数量: ${element.length}`);
        }
      } else {
        console.log(`❌ 未找到元素: ${selector}`);
      }
    }
    
    // 获取所有input元素的详细信息
    console.log('🔍 获取所有input元素信息...');
    const inputs = await page.evaluate(() => {
      const inputElements = document.querySelectorAll('input');
      return Array.from(inputElements).map(input => ({
        id: input.id,
        name: input.name,
        type: input.type,
        className: input.className,
        placeholder: input.placeholder
      }));
    });
    
    console.log('Input元素列表:');
    inputs.forEach((input, index) => {
      console.log(`  ${index + 1}. ID: ${input.id}, Name: ${input.name}, Type: ${input.type}, Class: ${input.className}, Placeholder: ${input.placeholder}`);
    });
    
    // 获取所有button元素的详细信息
    console.log('🔍 获取所有button元素信息...');
    const buttons = await page.evaluate(() => {
      const buttonElements = document.querySelectorAll('button');
      return Array.from(buttonElements).map(button => ({
        id: button.id,
        className: button.className,
        textContent: button.textContent.trim(),
        type: button.type
      }));
    });
    
    console.log('Button元素列表:');
    buttons.forEach((button, index) => {
      console.log(`  ${index + 1}. ID: ${button.id}, Class: ${button.className}, Text: ${button.textContent}, Type: ${button.type}`);
    });
    
    console.log('🎯 尝试手动等待用户操作...');
    console.log('请在浏览器中手动检查页面，按任意键继续...');
    
    // 等待用户输入
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

// 运行调试测试
debugBrowserTest().catch(console.error);