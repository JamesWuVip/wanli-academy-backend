#!/usr/bin/env node

/**
 * Token优化器 - 永久解决token过长问题的自动化工具
 * 用于监控和优化代码输出长度，提供最佳实践建议
 */

const fs = require('fs');
const path = require('path');

class TokenOptimizer {
  constructor() {
    this.config = this.loadConfig();
    this.stats = {
      totalCalls: 0,
      longOutputs: 0,
      optimizationsSuggested: 0
    };
  }

  loadConfig() {
    try {
      const configPath = path.join(__dirname, 'token-optimization-config.json');
      return JSON.parse(fs.readFileSync(configPath, 'utf8'));
    } catch (error) {
      console.warn('配置文件加载失败，使用默认配置');
      return this.getDefaultConfig();
    }
  }

  getDefaultConfig() {
    return {
      rules: {
        max_output_length: 2000,
        max_file_view_lines: 100,
        max_search_results: 10
      }
    };
  }

  // 检查输出长度
  checkOutputLength(content) {
    const length = content.length;
    this.stats.totalCalls++;
    
    if (length > this.config.rules.max_output_length) {
      this.stats.longOutputs++;
      return {
        isLong: true,
        length,
        suggestion: this.getSuggestion(length)
      };
    }
    
    return { isLong: false, length };
  }

  // 获取优化建议
  getSuggestion(length) {
    this.stats.optimizationsSuggested++;
    
    const suggestions = [
      '使用edit_file_update进行目标性修改',
      '指定具体的文件行范围进行查看',
      '使用更精确的搜索关键词',
      '合并相关操作减少工具调用',
      '避免输出完整文件内容'
    ];
    
    return suggestions[Math.floor(Math.random() * suggestions.length)];
  }

  // 优化文件查看参数
  optimizeFileView(filePath, totalLines) {
    const maxLines = this.config.rules.max_file_view_lines;
    
    if (totalLines <= maxLines) {
      return { startLine: 1, endLine: totalLines };
    }
    
    // 优先查看文件开头和关键部分
    return {
      startLine: 1,
      endLine: Math.min(maxLines, totalLines),
      suggestion: `文件过长(${totalLines}行)，建议分段查看`
    };
  }

  // 优化搜索参数
  optimizeSearch(query) {
    return {
      optimizedQuery: query.trim(),
      maxResults: this.config.rules.max_search_results,
      suggestion: '使用具体的类名或方法名进行搜索'
    };
  }

  // 生成优化报告
  generateReport() {
    const report = {
      timestamp: new Date().toISOString(),
      stats: this.stats,
      efficiency: {
        longOutputRate: (this.stats.longOutputs / this.stats.totalCalls * 100).toFixed(2) + '%',
        optimizationRate: (this.stats.optimizationsSuggested / this.stats.totalCalls * 100).toFixed(2) + '%'
      },
      recommendations: this.config.best_practices || []
    };
    
    return report;
  }

  // 保存优化报告
  saveReport() {
    const report = this.generateReport();
    const reportPath = path.join(__dirname, 'token-optimization-report.json');
    
    fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));
    console.log('Token优化报告已保存:', reportPath);
    
    return report;
  }
}

// 导出优化器实例
module.exports = new TokenOptimizer();

// 如果直接运行此脚本，生成报告
if (require.main === module) {
  const optimizer = new TokenOptimizer();
  const report = optimizer.saveReport();
  
  console.log('\n=== Token优化报告 ===');
  console.log(`总调用次数: ${report.stats.totalCalls}`);
  console.log(`长输出次数: ${report.stats.longOutputs}`);
  console.log(`长输出率: ${report.efficiency.longOutputRate}`);
  console.log(`优化建议率: ${report.efficiency.optimizationRate}`);
  
  console.log('\n=== 最佳实践建议 ===');
  report.recommendations.forEach((rec, index) => {
    console.log(`${index + 1}. ${rec}`);
  });
}