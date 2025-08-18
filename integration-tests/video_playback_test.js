const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');

// 测试配置
const config = {
    baseUrl: 'http://localhost:5173',
    timeout: 30000,
    screenshotDir: './screenshots',
    reportFile: './reports/video-playback-test-report.json'
};

// 确保目录存在
if (!fs.existsSync(config.screenshotDir)) {
    fs.mkdirSync(config.screenshotDir, { recursive: true });
}
if (!fs.existsSync('./reports')) {
    fs.mkdirSync('./reports', { recursive: true });
}

// 测试结果收集
let testResults = {
    timestamp: new Date().toISOString(),
    totalTests: 0,
    passedTests: 0,
    failedTests: 0,
    tests: []
};

// 辅助函数
function log(message) {
    const timestamp = new Date().toISOString();
    console.log(`[${timestamp}] ${message}`);
}

function addTestResult(testName, status, details = '', screenshot = '') {
    testResults.totalTests++;
    if (status === 'PASS') {
        testResults.passedTests++;
    } else {
        testResults.failedTests++;
    }
    
    testResults.tests.push({
        name: testName,
        status: status,
        details: details,
        screenshot: screenshot,
        timestamp: new Date().toISOString()
    });
}

async function takeScreenshot(page, name) {
    const timestamp = Date.now();
    const filename = `${name}_${timestamp}.png`;
    const filepath = path.join(config.screenshotDir, filename);
    await page.screenshot({ path: filepath, fullPage: true });
    return filename;
}

// 等待元素出现
async function waitForElement(page, selector, timeout = 10000) {
    try {
        await page.waitForSelector(selector, { timeout });
        return true;
    } catch (error) {
        log(`等待元素失败: ${selector} - ${error.message}`);
        return false;
    }
}

// 主测试函数
async function runVideoPlaybackTests() {
    let browser;
    let page;
    
    try {
        log('启动浏览器...');
        browser = await puppeteer.launch({
            headless: false,
            defaultViewport: { width: 1280, height: 720 },
            args: ['--no-sandbox', '--disable-setuid-sandbox']
        });
        
        page = await browser.newPage();
        await page.setDefaultTimeout(config.timeout);
        
        // 测试1: 访问首页
        log('测试1: 访问应用首页');
        try {
            await page.goto(config.baseUrl, { waitUntil: 'networkidle2' });
            const screenshot = await takeScreenshot(page, 'homepage');
            addTestResult('访问首页', 'PASS', '成功加载应用首页', screenshot);
        } catch (error) {
            const screenshot = await takeScreenshot(page, 'homepage_error');
            addTestResult('访问首页', 'FAIL', `无法访问首页: ${error.message}`, screenshot);
            throw error;
        }
        
        // 测试2: 学员登录
        log('测试2: 学员登录流程');
        try {
            // 点击学员登录按钮
            const studentLoginExists = await waitForElement(page, 'button:has-text("学员登录")');
            if (!studentLoginExists) {
                throw new Error('找不到学员登录按钮');
            }
            
            await page.click('button:has-text("学员登录")');
            await page.waitForTimeout(2000);
            
            // 填写登录信息
            await page.fill('input[placeholder="请输入用户名"]', 'student1');
            await page.fill('input[placeholder="请输入密码"]', 'password123');
            
            const loginScreenshot = await takeScreenshot(page, 'login_form');
            
            // 点击登录
            await page.click('button:has-text("登录")');
            await page.waitForTimeout(3000);
            
            // 验证登录成功
            const isLoggedIn = await page.evaluate(() => {
                return document.querySelector('.assignment-list') !== null ||
                       document.querySelector('h2:has-text("我的作业")') !== null ||
                       window.location.pathname.includes('/assignments');
            });
            
            if (isLoggedIn) {
                const successScreenshot = await takeScreenshot(page, 'login_success');
                addTestResult('学员登录', 'PASS', '学员登录成功，进入作业列表页面', successScreenshot);
            } else {
                const failScreenshot = await takeScreenshot(page, 'login_fail');
                addTestResult('学员登录', 'FAIL', '登录后未能正确跳转到作业列表页面', failScreenshot);
            }
        } catch (error) {
            const errorScreenshot = await takeScreenshot(page, 'login_error');
            addTestResult('学员登录', 'FAIL', `登录过程出错: ${error.message}`, errorScreenshot);
        }
        
        // 测试3: 查看作业列表
        log('测试3: 验证作业列表显示');
        try {
            // 等待作业列表加载
            const listExists = await waitForElement(page, '.assignment-item, .assignment-card, [class*="assignment"]');
            
            if (listExists) {
                const assignments = await page.evaluate(() => {
                    const items = document.querySelectorAll('.assignment-item, .assignment-card, [class*="assignment"]');
                    return Array.from(items).map(item => ({
                        text: item.textContent.trim(),
                        hasButton: item.querySelector('button') !== null
                    }));
                });
                
                const listScreenshot = await takeScreenshot(page, 'assignment_list');
                
                if (assignments.length > 0) {
                    addTestResult('作业列表显示', 'PASS', `成功显示${assignments.length}个作业项目`, listScreenshot);
                } else {
                    addTestResult('作业列表显示', 'FAIL', '作业列表为空', listScreenshot);
                }
            } else {
                const noListScreenshot = await takeScreenshot(page, 'no_assignment_list');
                addTestResult('作业列表显示', 'FAIL', '未找到作业列表元素', noListScreenshot);
            }
        } catch (error) {
            const errorScreenshot = await takeScreenshot(page, 'assignment_list_error');
            addTestResult('作业列表显示', 'FAIL', `作业列表加载出错: ${error.message}`, errorScreenshot);
        }
        
        // 测试4: 点击查看结果
        log('测试4: 点击查看作业结果');
        try {
            // 查找并点击第一个"查看结果"按钮
            const resultButtonExists = await waitForElement(page, 'button:has-text("查看结果")');
            
            if (resultButtonExists) {
                await page.click('button:has-text("查看结果")');
                await page.waitForTimeout(3000);
                
                // 验证是否进入结果页面
                const isResultPage = await page.evaluate(() => {
                    return document.querySelector('.submission-result') !== null ||
                           document.querySelector('h2:has-text("作业结果")') !== null ||
                           window.location.pathname.includes('/result');
                });
                
                const resultScreenshot = await takeScreenshot(page, 'result_page');
                
                if (isResultPage) {
                    addTestResult('查看结果导航', 'PASS', '成功进入作业结果页面', resultScreenshot);
                } else {
                    addTestResult('查看结果导航', 'FAIL', '点击查看结果后未能正确跳转', resultScreenshot);
                }
            } else {
                const noButtonScreenshot = await takeScreenshot(page, 'no_result_button');
                addTestResult('查看结果导航', 'FAIL', '未找到查看结果按钮', noButtonScreenshot);
            }
        } catch (error) {
            const errorScreenshot = await takeScreenshot(page, 'result_navigation_error');
            addTestResult('查看结果导航', 'FAIL', `查看结果导航出错: ${error.message}`, errorScreenshot);
        }
        
        // 测试5: 视频播放器检测
        log('测试5: 检测视频播放器组件');
        try {
            // 查找视频播放器元素
            const videoPlayerExists = await page.evaluate(() => {
                const videoElement = document.querySelector('video');
                const videoContainer = document.querySelector('.video-player, [class*="video"]');
                return {
                    hasVideoElement: videoElement !== null,
                    hasVideoContainer: videoContainer !== null,
                    videoSrc: videoElement ? videoElement.src : null,
                    videoControls: videoElement ? videoElement.hasAttribute('controls') : false
                };
            });
            
            const videoScreenshot = await takeScreenshot(page, 'video_player_check');
            
            if (videoPlayerExists.hasVideoElement || videoPlayerExists.hasVideoContainer) {
                let details = '发现视频播放器组件';
                if (videoPlayerExists.videoSrc) {
                    details += `，视频源: ${videoPlayerExists.videoSrc}`;
                }
                if (videoPlayerExists.videoControls) {
                    details += '，包含播放控制';
                }
                addTestResult('视频播放器检测', 'PASS', details, videoScreenshot);
            } else {
                addTestResult('视频播放器检测', 'FAIL', '未找到视频播放器组件', videoScreenshot);
            }
        } catch (error) {
            const errorScreenshot = await takeScreenshot(page, 'video_detection_error');
            addTestResult('视频播放器检测', 'FAIL', `视频播放器检测出错: ${error.message}`, errorScreenshot);
        }
        
        // 测试6: 视频播放功能
        log('测试6: 测试视频播放功能');
        try {
            const videoElement = await page.$('video');
            
            if (videoElement) {
                // 检查视频是否可以播放
                const videoInfo = await page.evaluate(() => {
                    const video = document.querySelector('video');
                    if (!video) return null;
                    
                    return {
                        src: video.src,
                        duration: video.duration,
                        paused: video.paused,
                        readyState: video.readyState,
                        networkState: video.networkState
                    };
                });
                
                if (videoInfo && videoInfo.src) {
                    // 尝试播放视频
                    await page.evaluate(() => {
                        const video = document.querySelector('video');
                        if (video) {
                            video.play().catch(e => console.log('播放失败:', e));
                        }
                    });
                    
                    await page.waitForTimeout(2000);
                    
                    // 检查播放状态
                    const playbackStatus = await page.evaluate(() => {
                        const video = document.querySelector('video');
                        return {
                            isPlaying: !video.paused,
                            currentTime: video.currentTime,
                            error: video.error ? video.error.message : null
                        };
                    });
                    
                    const playbackScreenshot = await takeScreenshot(page, 'video_playback');
                    
                    if (playbackStatus.isPlaying || playbackStatus.currentTime > 0) {
                        addTestResult('视频播放功能', 'PASS', `视频播放正常，当前时间: ${playbackStatus.currentTime}秒`, playbackScreenshot);
                    } else if (playbackStatus.error) {
                        addTestResult('视频播放功能', 'FAIL', `视频播放错误: ${playbackStatus.error}`, playbackScreenshot);
                    } else {
                        addTestResult('视频播放功能', 'FAIL', '视频无法播放', playbackScreenshot);
                    }
                } else {
                    const noSrcScreenshot = await takeScreenshot(page, 'video_no_src');
                    addTestResult('视频播放功能', 'FAIL', '视频元素没有有效的源地址', noSrcScreenshot);
                }
            } else {
                const noVideoScreenshot = await takeScreenshot(page, 'no_video_element');
                addTestResult('视频播放功能', 'FAIL', '页面中没有视频元素', noVideoScreenshot);
            }
        } catch (error) {
            const errorScreenshot = await takeScreenshot(page, 'video_playback_error');
            addTestResult('视频播放功能', 'FAIL', `视频播放测试出错: ${error.message}`, errorScreenshot);
        }
        
    } catch (error) {
        log(`测试执行出错: ${error.message}`);
        if (page) {
            const errorScreenshot = await takeScreenshot(page, 'test_error');
            addTestResult('测试执行', 'FAIL', `测试执行出错: ${error.message}`, errorScreenshot);
        }
    } finally {
        if (browser) {
            await browser.close();
        }
        
        // 生成测试报告
        log('生成测试报告...');
        testResults.summary = {
            successRate: testResults.totalTests > 0 ? (testResults.passedTests / testResults.totalTests * 100).toFixed(2) + '%' : '0%',
            duration: 'N/A'
        };
        
        // 保存测试报告
        fs.writeFileSync(config.reportFile, JSON.stringify(testResults, null, 2));
        
        // 输出测试结果摘要
        log('\n=== 视频播放功能测试报告 ===');
        log(`总测试数: ${testResults.totalTests}`);
        log(`通过测试: ${testResults.passedTests}`);
        log(`失败测试: ${testResults.failedTests}`);
        log(`成功率: ${testResults.summary.successRate}`);
        log(`报告文件: ${config.reportFile}`);
        
        testResults.tests.forEach((test, index) => {
            log(`${index + 1}. ${test.name}: ${test.status}`);
            if (test.details) {
                log(`   详情: ${test.details}`);
            }
        });
        
        return testResults;
    }
}

// 如果直接运行此脚本
if (require.main === module) {
    runVideoPlaybackTests()
        .then(results => {
            process.exit(results.failedTests > 0 ? 1 : 0);
        })
        .catch(error => {
            console.error('测试运行失败:', error);
            process.exit(1);
        });
}

module.exports = { runVideoPlaybackTests };
