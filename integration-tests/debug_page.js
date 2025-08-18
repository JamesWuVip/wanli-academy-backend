const puppeteer = require('puppeteer');

(async () => {
    const browser = await puppeteer.launch({
        headless: false,
        defaultViewport: { width: 1280, height: 720 },
        args: ['--no-sandbox', '--disable-setuid-sandbox']
    });
    
    const page = await browser.newPage();
    
    try {
        // 先登录
        console.log('导航到登录页面...');
        await page.goto('http://localhost:5173/', { waitUntil: 'networkidle2' });
        
        console.log('执行登录...');
        await page.waitForSelector('#username', { timeout: 10000 });
        await page.click('#username');
        await page.evaluate(() => document.querySelector('#username').value = '');
        await page.type('#username', 'test_student1', { delay: 100 });
        
        await page.waitForSelector('#password', { timeout: 5000 });
        await page.click('#password');
        await page.evaluate(() => document.querySelector('#password').value = '');
        await page.type('#password', 'password123', { delay: 100 });
        
        console.log('点击登录按钮...');
        await page.waitForSelector('.login-btn', { timeout: 5000 });
        await page.click('.login-btn');
        
        // 等待登录完成，检查是否跳转
        console.log('等待登录完成...');
        await new Promise(resolve => setTimeout(resolve, 5000));
        
        const currentUrl = page.url();
        console.log('登录后当前URL:', currentUrl);
        
        // 如果还在登录页面，说明登录失败
        if (currentUrl === 'http://localhost:5173/') {
            console.log('登录可能失败，检查页面错误信息...');
            const errorMsg = await page.evaluate(() => {
                const errorEl = document.querySelector('.error-message, .alert-danger, .error');
                return errorEl ? errorEl.textContent : '无错误信息';
            });
        
            console.log('错误信息:', errorMsg);
        }
        
        // 监听网络请求和页面错误（在导航前开始监听）
        const responses = [];
        const requests = [];
        const consoleMessages = [];
        const pageErrors = [];
        
        page.on('request', request => {
            if (request.url().includes('/api/')) {
                requests.push({
                    url: request.url(),
                    method: request.method(),
                    headers: request.headers()
                });
            }
        });
        
        page.on('response', async response => {
            if (response.url().includes('/api/')) {
                let responseData = null;
                try {
                    responseData = await response.json();
                } catch (e) {
                    responseData = 'Failed to parse JSON';
                }
                
                responses.push({
                    url: response.url(),
                    status: response.status(),
                    statusText: response.statusText(),
                    data: responseData
                });
            }
        });
        
        page.on('console', msg => {
            consoleMessages.push(`${msg.type()}: ${msg.text()}`);
        });
        
        page.on('pageerror', error => {
            pageErrors.push(error.message);
        });
        
        // 导航到结果页面
        console.log('导航到结果页面...');
        await page.goto('http://localhost:5173/submissions/770e8400-e29b-41d4-a716-446655440001/result', {
            waitUntil: 'networkidle2'
        });
        
        // 等待页面加载
        await new Promise(resolve => setTimeout(resolve, 3000));
        
        // 等待一下让错误和日志收集
         await new Promise(resolve => setTimeout(resolve, 1000));
         
         // 检查页面上是否有Vue应用
         const vueAppExists = await page.evaluate(() => {
             return !!document.querySelector('#app');
         });
        
        // 获取页面文本内容
        const pageText = await page.evaluate(() => document.body.innerText);
        console.log('\n页面文本内容:');
        console.log(pageText);
        
        // 检查页面状态
        const errorState = await page.$('.error') !== null;
        const loadingState = await page.$('.loading') !== null;
        const videoPlayer = await page.$('.video-player') !== null;
        const videoArea = await page.$('.video-container, .video-element, .no-video') !== null;
        
        console.log('\n页面状态:');
        console.log('错误状态:', errorState);
        console.log('加载状态:', loadingState);
        console.log('视频播放器:', videoPlayer);
        console.log('视频区域:', videoArea);
        
        // 检查内容状态
        const resultContent = await page.$('.result-content') !== null;
        const questionArea = await page.$('.question-card, .question-item') !== null;
        
        console.log('\n内容状态:');
        console.log('结果内容:', resultContent);
        console.log('题目区域:', questionArea);
        
        // 检查具体的DOM元素
        const domElements = await page.evaluate(() => {
            const elements = {};
            elements.summaryCard = document.querySelector('.summary-card') !== null;
            elements.questionCards = document.querySelectorAll('.question-card').length;
            elements.videoPlayers = document.querySelectorAll('.video-player').length;
            elements.errorMessages = document.querySelectorAll('.error-message').length;
            elements.loadingSpinners = document.querySelectorAll('.loading').length;
            return elements;
        });
        
        console.log('\nDOM元素检查:');
        console.log('摘要卡片:', domElements.summaryCard);
        console.log('题目卡片数量:', domElements.questionCards);
        console.log('视频播放器数量:', domElements.videoPlayers);
        console.log('错误信息数量:', domElements.errorMessages);
        console.log('加载动画数量:', domElements.loadingSpinners);
        
        // 显示网络请求
        console.log('\n网络请求:');
        console.log('发出的请求:');
        requests.forEach(request => {
            console.log(`${request.method} ${request.url}`);
            if (request.headers.authorization) {
                console.log(`  Authorization: ${request.headers.authorization.substring(0, 20)}...`);
            }
        });
        
        console.log('\n收到的响应:');
        responses.forEach(response => {
            console.log(`${response.status} ${response.statusText} - ${response.url}`);
            if (response.data && response.data !== 'Failed to parse JSON') {
                console.log('  响应数据:', JSON.stringify(response.data, null, 2));
            }
        });
        
        // 显示调试信息
        console.log('\n调试信息:');
        console.log('Vue应用元素存在:', vueAppExists);
        
        if (pageErrors.length > 0) {
            console.log('\n页面错误:');
            pageErrors.forEach(error => console.log('  -', error));
        }
        
        if (consoleMessages.length > 0) {
            console.log('\n控制台消息:');
            consoleMessages.forEach(msg => console.log('  -', msg));
        }
        
    } catch (error) {
        console.error('调试过程中出错:', error);
    } finally {
        await browser.close();
    }
})();