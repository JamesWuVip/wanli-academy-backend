const puppeteer = require('puppeteer');

const config = {
  frontendURL: 'http://localhost:5173'
};

async function checkLoginSelectors() {
  const browser = await puppeteer.launch({ 
    headless: false,
    defaultViewport: null,
    args: ['--start-maximized']
  });
  
  const page = await browser.newPage();
  
  try {
    console.log('检查登录页面选择器...');
    
    // 访问首页
    await page.goto(config.frontendURL);
    await new Promise(resolve => setTimeout(resolve, 3000));
    
    console.log('当前页面URL:', page.url());
    console.log('页面标题:', await page.title());
    
    // 检查页面上的所有输入框和按钮
    const pageElements = await page.evaluate(() => {
      const inputs = Array.from(document.querySelectorAll('input')).map(input => ({
        type: input.type,
        placeholder: input.placeholder,
        testId: input.getAttribute('data-testid'),
        id: input.id,
        name: input.name,
        className: input.className
      }));
      
      const buttons = Array.from(document.querySelectorAll('button')).map(button => ({
        text: button.textContent.trim(),
        testId: button.getAttribute('data-testid'),
        id: button.id,
        className: button.className
      }));
      
      const allTestIds = Array.from(document.querySelectorAll('[data-testid]')).map(el => ({
        tagName: el.tagName,
        testId: el.getAttribute('data-testid'),
        text: el.textContent.trim().substring(0, 50)
      }));
      
      return {
        inputs,
        buttons,
        allTestIds,
        bodyHTML: document.body.innerHTML.substring(0, 2000)
      };
    });
    
    console.log('\n=== 页面元素分析 ===');
    console.log('输入框:', pageElements.inputs);
    console.log('\n按钮:', pageElements.buttons);
    console.log('\n所有测试ID:', pageElements.allTestIds);
    
    // 截图
    await page.screenshot({ path: './login_page_debug.png', fullPage: true });
    console.log('\n截图已保存到: ./login_page_debug.png');
    
  } catch (error) {
    console.error('检查过程中发生错误:', error);
  } finally {
    await browser.close();
  }
}

checkLoginSelectors().catch(console.error);