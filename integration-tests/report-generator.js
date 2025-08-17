#!/usr/bin/env node

/**
 * 万里书院集成测试报告生成器
 * 将测试结果转换为HTML和JSON格式的详细报告
 */

const fs = require('fs');
const path = require('path');

// 报告生成器配置
const config = {
    reportDir: process.env.REPORT_DIR || 'integration-tests/reports',
    inputFile: 'integration-test-results.json',
    outputHtml: 'integration-test-report.html',
    outputJson: 'integration-test-report.json'
};

// HTML模板
const htmlTemplate = `
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>万里书院集成测试报告</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            line-height: 1.6;
            color: #333;
            background-color: #f5f5f5;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
        }
        
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            border-radius: 10px;
            margin-bottom: 30px;
            text-align: center;
        }
        
        .header h1 {
            font-size: 2.5em;
            margin-bottom: 10px;
        }
        
        .header .subtitle {
            font-size: 1.2em;
            opacity: 0.9;
        }
        
        .summary {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .summary-card {
            background: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            text-align: center;
            transition: transform 0.2s;
        }
        
        .summary-card:hover {
            transform: translateY(-2px);
        }
        
        .summary-card .number {
            font-size: 2.5em;
            font-weight: bold;
            margin-bottom: 10px;
        }
        
        .summary-card .label {
            color: #666;
            font-size: 0.9em;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        
        .passed { color: #28a745; }
        .failed { color: #dc3545; }
        .skipped { color: #ffc107; }
        .total { color: #007bff; }
        
        .test-suites {
            display: grid;
            gap: 20px;
        }
        
        .test-suite {
            background: white;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            overflow: hidden;
        }
        
        .suite-header {
            padding: 20px;
            background: #f8f9fa;
            border-bottom: 1px solid #dee2e6;
            cursor: pointer;
            transition: background-color 0.2s;
        }
        
        .suite-header:hover {
            background: #e9ecef;
        }
        
        .suite-header h3 {
            margin-bottom: 5px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        
        .suite-stats {
            display: flex;
            gap: 15px;
            font-size: 0.9em;
            color: #666;
        }
        
        .suite-content {
            padding: 20px;
            display: none;
        }
        
        .suite-content.active {
            display: block;
        }
        
        .test-item {
            display: flex;
            align-items: center;
            padding: 10px 0;
            border-bottom: 1px solid #f0f0f0;
        }
        
        .test-item:last-child {
            border-bottom: none;
        }
        
        .test-status {
            width: 20px;
            height: 20px;
            border-radius: 50%;
            margin-right: 15px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 12px;
            color: white;
            font-weight: bold;
        }
        
        .test-status.passed {
            background: #28a745;
        }
        
        .test-status.failed {
            background: #dc3545;
        }
        
        .test-status.skipped {
            background: #ffc107;
        }
        
        .test-name {
            flex: 1;
            font-weight: 500;
        }
        
        .test-message {
            color: #666;
            font-size: 0.9em;
            margin-left: 35px;
            margin-top: 5px;
        }
        
        .test-error {
            background: #f8d7da;
            color: #721c24;
            padding: 10px;
            border-radius: 5px;
            margin: 10px 0;
            font-family: monospace;
            font-size: 0.85em;
            white-space: pre-wrap;
        }
        
        .progress-bar {
            width: 100%;
            height: 8px;
            background: #e9ecef;
            border-radius: 4px;
            overflow: hidden;
            margin: 20px 0;
        }
        
        .progress-fill {
            height: 100%;
            background: linear-gradient(90deg, #28a745, #20c997);
            transition: width 0.3s ease;
        }
        
        .metadata {
            background: white;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            margin-top: 30px;
        }
        
        .metadata h3 {
            margin-bottom: 15px;
            color: #495057;
        }
        
        .metadata-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
        }
        
        .metadata-item {
            display: flex;
            justify-content: space-between;
            padding: 8px 0;
            border-bottom: 1px solid #f0f0f0;
        }
        
        .metadata-item:last-child {
            border-bottom: none;
        }
        
        .metadata-label {
            font-weight: 500;
            color: #666;
        }
        
        .metadata-value {
            color: #333;
        }
        
        .toggle-icon {
            transition: transform 0.2s;
        }
        
        .toggle-icon.rotated {
            transform: rotate(180deg);
        }
        
        @media (max-width: 768px) {
            .container {
                padding: 10px;
            }
            
            .header {
                padding: 20px;
            }
            
            .header h1 {
                font-size: 2em;
            }
            
            .summary {
                grid-template-columns: repeat(2, 1fr);
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>万里书院集成测试报告</h1>
            <div class="subtitle">Integration Test Report</div>
        </div>
        
        <div class="summary">
            <div class="summary-card">
                <div class="number total">{{TOTAL_TESTS}}</div>
                <div class="label">总测试数</div>
            </div>
            <div class="summary-card">
                <div class="number passed">{{PASSED_TESTS}}</div>
                <div class="label">通过测试</div>
            </div>
            <div class="summary-card">
                <div class="number failed">{{FAILED_TESTS}}</div>
                <div class="label">失败测试</div>
            </div>
            <div class="summary-card">
                <div class="number skipped">{{SKIPPED_TESTS}}</div>
                <div class="label">跳过测试</div>
            </div>
            <div class="summary-card">
                <div class="number total">{{SUCCESS_RATE}}%</div>
                <div class="label">成功率</div>
            </div>
            <div class="summary-card">
                <div class="number total">{{DURATION}}</div>
                <div class="label">执行时间</div>
            </div>
        </div>
        
        <div class="progress-bar">
            <div class="progress-fill" style="width: {{SUCCESS_RATE}}%"></div>
        </div>
        
        <div class="test-suites">
            {{TEST_SUITES}}
        </div>
        
        <div class="metadata">
            <h3>测试元数据</h3>
            <div class="metadata-grid">
                <div class="metadata-item">
                    <span class="metadata-label">开始时间:</span>
                    <span class="metadata-value">{{START_TIME}}</span>
                </div>
                <div class="metadata-item">
                    <span class="metadata-label">结束时间:</span>
                    <span class="metadata-value">{{END_TIME}}</span>
                </div>
                <div class="metadata-item">
                    <span class="metadata-label">执行时长:</span>
                    <span class="metadata-value">{{DURATION_MS}}ms</span>
                </div>
                <div class="metadata-item">
                    <span class="metadata-label">测试套件数:</span>
                    <span class="metadata-value">{{SUITE_COUNT}}</span>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        // 切换测试套件展开/折叠
        document.querySelectorAll('.suite-header').forEach(header => {
            header.addEventListener('click', () => {
                const content = header.nextElementSibling;
                const icon = header.querySelector('.toggle-icon');
                
                content.classList.toggle('active');
                icon.classList.toggle('rotated');
            });
        });
        
        // 默认展开第一个失败的测试套件
        const failedSuite = document.querySelector('.test-suite.has-failures');
        if (failedSuite) {
            const header = failedSuite.querySelector('.suite-header');
            const content = failedSuite.querySelector('.suite-content');
            const icon = failedSuite.querySelector('.toggle-icon');
            
            content.classList.add('active');
            icon.classList.add('rotated');
        }
    </script>
</body>
</html>
`;

// 报告生成器类
class ReportGenerator {
    constructor(reportDir) {
        this.reportDir = reportDir;
        this.inputPath = path.join(reportDir, config.inputFile);
        this.htmlOutputPath = path.join(reportDir, config.outputHtml);
        this.jsonOutputPath = path.join(reportDir, config.outputJson);
    }

    // 读取测试结果
    readTestResults() {
        try {
            if (!fs.existsSync(this.inputPath)) {
                throw new Error(`测试结果文件不存在: ${this.inputPath}`);
            }
            
            const data = fs.readFileSync(this.inputPath, 'utf8');
            return JSON.parse(data);
        } catch (error) {
            throw new Error(`读取测试结果失败: ${error.message}`);
        }
    }

    // 格式化时间
    formatTime(isoString) {
        if (!isoString) return 'N/A';
        return new Date(isoString).toLocaleString('zh-CN');
    }

    // 格式化持续时间
    formatDuration(ms) {
        if (!ms) return 'N/A';
        
        if (ms < 1000) {
            return `${ms}ms`;
        } else if (ms < 60000) {
            return `${(ms / 1000).toFixed(1)}s`;
        } else {
            const minutes = Math.floor(ms / 60000);
            const seconds = Math.floor((ms % 60000) / 1000);
            return `${minutes}m ${seconds}s`;
        }
    }

    // 生成测试套件HTML
    generateTestSuiteHtml(suite) {
        const hasFailures = suite.failed > 0;
        const suiteClass = hasFailures ? 'test-suite has-failures' : 'test-suite';
        
        const testsHtml = suite.tests.map(test => {
            const statusIcon = {
                passed: '✓',
                failed: '✗',
                skipped: '⏭'
            }[test.status] || '?';
            
            let testHtml = `
                <div class="test-item">
                    <div class="test-status ${test.status}">${statusIcon}</div>
                    <div class="test-name">${this.escapeHtml(test.name)}</div>
                </div>
            `;
            
            if (test.message) {
                testHtml += `<div class="test-message">${this.escapeHtml(test.message)}</div>`;
            }
            
            if (test.status === 'failed' && test.details && test.details.error) {
                testHtml += `<div class="test-error">${this.escapeHtml(test.details.error)}</div>`;
            }
            
            return testHtml;
        }).join('');
        
        return `
            <div class="${suiteClass}">
                <div class="suite-header">
                    <h3>
                        ${this.escapeHtml(suite.name)}
                        <span class="toggle-icon">▼</span>
                    </h3>
                    <div class="suite-stats">
                        <span class="passed">✓ ${suite.passed}</span>
                        <span class="failed">✗ ${suite.failed}</span>
                        <span class="skipped">⏭ ${suite.skipped}</span>
                        <span>⏱ ${this.formatDuration(suite.duration_ms)}</span>
                    </div>
                    <div style="margin-top: 5px; color: #666; font-size: 0.9em;">
                        ${this.escapeHtml(suite.description)}
                    </div>
                </div>
                <div class="suite-content">
                    ${testsHtml}
                </div>
            </div>
        `;
    }

    // HTML转义
    escapeHtml(text) {
        if (!text) return '';
        const div = { innerHTML: '' };
        div.textContent = text;
        return div.innerHTML || text.toString()
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    // 生成HTML报告
    generateHtmlReport(results) {
        const testSuitesHtml = results.test_suites.map(suite => 
            this.generateTestSuiteHtml(suite)
        ).join('');
        
        let html = htmlTemplate
            .replace(/{{TOTAL_TESTS}}/g, results.summary.total_tests)
            .replace(/{{PASSED_TESTS}}/g, results.summary.passed_tests)
            .replace(/{{FAILED_TESTS}}/g, results.summary.failed_tests)
            .replace(/{{SKIPPED_TESTS}}/g, results.summary.skipped_tests)
            .replace(/{{SUCCESS_RATE}}/g, results.summary.success_rate)
            .replace(/{{DURATION}}/g, this.formatDuration(results.summary.duration_ms))
            .replace(/{{TEST_SUITES}}/g, testSuitesHtml)
            .replace(/{{START_TIME}}/g, this.formatTime(results.summary.start_time))
            .replace(/{{END_TIME}}/g, this.formatTime(results.summary.end_time))
            .replace(/{{DURATION_MS}}/g, results.summary.duration_ms)
            .replace(/{{SUITE_COUNT}}/g, results.test_suites.length);
        
        return html;
    }

    // 生成增强的JSON报告
    generateEnhancedJsonReport(results) {
        const enhancedResults = {
            ...results,
            metadata: {
                generator: 'Wanli Academy Integration Test Report Generator',
                version: '1.0.0',
                generated_at: new Date().toISOString(),
                report_format: 'enhanced_json_v1'
            },
            analysis: {
                most_failed_suite: null,
                fastest_suite: null,
                slowest_suite: null,
                error_patterns: []
            }
        };

        // 分析测试套件
        if (results.test_suites && results.test_suites.length > 0) {
            // 找出失败最多的套件
            enhancedResults.analysis.most_failed_suite = results.test_suites
                .reduce((prev, current) => (prev.failed > current.failed) ? prev : current);
            
            // 找出最快和最慢的套件
            enhancedResults.analysis.fastest_suite = results.test_suites
                .reduce((prev, current) => (prev.duration_ms < current.duration_ms) ? prev : current);
            
            enhancedResults.analysis.slowest_suite = results.test_suites
                .reduce((prev, current) => (prev.duration_ms > current.duration_ms) ? prev : current);
            
            // 分析错误模式
            const errorMessages = [];
            results.test_suites.forEach(suite => {
                suite.tests.forEach(test => {
                    if (test.status === 'failed' && test.details && test.details.error) {
                        errorMessages.push(test.details.error);
                    }
                });
            });
            
            // 统计错误类型
            const errorCounts = {};
            errorMessages.forEach(error => {
                const errorType = this.categorizeError(error);
                errorCounts[errorType] = (errorCounts[errorType] || 0) + 1;
            });
            
            enhancedResults.analysis.error_patterns = Object.entries(errorCounts)
                .map(([type, count]) => ({ type, count }))
                .sort((a, b) => b.count - a.count);
        }

        return enhancedResults;
    }

    // 错误分类
    categorizeError(error) {
        const errorLower = error.toLowerCase();
        
        if (errorLower.includes('timeout') || errorLower.includes('超时')) {
            return 'timeout';
        } else if (errorLower.includes('connection') || errorLower.includes('连接')) {
            return 'connection';
        } else if (errorLower.includes('401') || errorLower.includes('unauthorized')) {
            return 'authentication';
        } else if (errorLower.includes('403') || errorLower.includes('forbidden')) {
            return 'authorization';
        } else if (errorLower.includes('404') || errorLower.includes('not found')) {
            return 'not_found';
        } else if (errorLower.includes('500') || errorLower.includes('internal server')) {
            return 'server_error';
        } else {
            return 'other';
        }
    }

    // 生成所有报告
    async generateReports() {
        try {
            console.log('🔄 开始生成测试报告...');
            
            // 确保报告目录存在
            if (!fs.existsSync(this.reportDir)) {
                fs.mkdirSync(this.reportDir, { recursive: true });
            }
            
            // 读取测试结果
            console.log(`📖 读取测试结果: ${this.inputPath}`);
            const results = this.readTestResults();
            
            // 生成HTML报告
            console.log('🎨 生成HTML报告...');
            const htmlReport = this.generateHtmlReport(results);
            fs.writeFileSync(this.htmlOutputPath, htmlReport, 'utf8');
            console.log(`✅ HTML报告已生成: ${this.htmlOutputPath}`);
            
            // 生成增强JSON报告
            console.log('📊 生成增强JSON报告...');
            const enhancedResults = this.generateEnhancedJsonReport(results);
            fs.writeFileSync(this.jsonOutputPath, JSON.stringify(enhancedResults, null, 2), 'utf8');
            console.log(`✅ JSON报告已生成: ${this.jsonOutputPath}`);
            
            // 显示报告摘要
            console.log('\n📋 报告摘要:');
            console.log(`   总测试数: ${results.summary.total_tests}`);
            console.log(`   通过测试: ${results.summary.passed_tests}`);
            console.log(`   失败测试: ${results.summary.failed_tests}`);
            console.log(`   跳过测试: ${results.summary.skipped_tests}`);
            console.log(`   成功率: ${results.summary.success_rate}%`);
            console.log(`   执行时间: ${this.formatDuration(results.summary.duration_ms)}`);
            
            if (enhancedResults.analysis.error_patterns.length > 0) {
                console.log('\n🔍 错误分析:');
                enhancedResults.analysis.error_patterns.forEach(pattern => {
                    console.log(`   ${pattern.type}: ${pattern.count}次`);
                });
            }
            
            console.log('\n🎉 报告生成完成!');
            return true;
            
        } catch (error) {
            console.error('❌ 报告生成失败:', error.message);
            console.error(error.stack);
            return false;
        }
    }
}

// 主函数
async function main() {
    console.log('📊 万里书院集成测试报告生成器');
    console.log('='.repeat(50));
    
    const reportDir = config.reportDir;
    console.log(`报告目录: ${reportDir}`);
    
    const generator = new ReportGenerator(reportDir);
    const success = await generator.generateReports();
    
    process.exit(success ? 0 : 1);
}

// 程序入口
if (require.main === module) {
    main();
}

module.exports = {
    ReportGenerator
};