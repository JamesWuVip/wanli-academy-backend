# Token优化指南 - 永久解决方案

## 问题分析

Token过长问题的根本原因：
1. **代码输出冗长** - 输出完整文件内容而非必要部分
2. **重复性工具调用** - 多次查看相同文件或执行类似操作
3. **大文件处理不当** - 一次性读取大型文件
4. **搜索范围过广** - 使用模糊搜索导致大量结果
5. **缺乏增量修改策略** - 重写整个文件而非局部修改

## 永久解决方案

### 1. 代码编辑优化

**优先使用 `edit_file_update`**
```javascript
// ✅ 好的做法 - 目标性修改
{
  "name": "edit_file_update",
  "params": {
    "file_path": "/path/to/file.js",
    "replace_blocks": [{
      "old_str": "具体的代码片段",
      "new_str": "修改后的代码"
    }]
  }
}

// ❌ 避免的做法 - 输出完整文件
{
  "name": "edit_file_fast_apply",
  "params": {
    "content": "整个文件的内容...(数千行)"
  }
}
```

**限制替换块数量**
- 每次最多5个replace_blocks
- 每个block保持简洁，只包含必要的上下文

### 2. 文件查看优化

**使用精确的行范围**
```javascript
// ✅ 好的做法 - 指定行范围
{
  "name": "view_files",
  "params": {
    "files": [{
      "file_path": "/path/to/file.js",
      "start_line_one_indexed": 50,
      "end_line_one_indexed_inclusive": 100
    }]
  }
}

// ❌ 避免的做法 - 读取整个文件
{
  "name": "view_files",
  "params": {
    "files": [{
      "file_path": "/path/to/file.js",
      "read_entire_file": true
    }]
  }
}
```

### 3. 搜索优化

**使用具体的搜索词**
```javascript
// ✅ 好的做法 - 精确搜索
{
  "name": "search_codebase",
  "params": {
    "information_request": "SubmissionService中的getSubmissionResult方法",
    "target_directories": ["/specific/directory"]
  }
}

// ❌ 避免的做法 - 模糊搜索
{
  "name": "search_codebase",
  "params": {
    "information_request": "所有相关的代码"
  }
}
```

### 4. 工具调用策略

**合并相关操作**
- 一次性查看多个相关文件
- 批量处理相似的修改
- 避免重复的状态检查

**减少不必要的调用**
- 缓存已获取的信息
- 避免重复查看相同文件
- 合理使用搜索结果

### 5. 输出长度控制

**设置硬性限制**
- 单次输出不超过2000字符
- 文件查看不超过100行
- 搜索结果不超过10条

**使用省略策略**
```javascript
// 对于长内容使用省略
const content = longContent.length > 500 
  ? longContent.substring(0, 500) + '...(内容已截断)'
  : longContent;
```

## 实施检查清单

### 开发前检查
- [ ] 确定最小修改范围
- [ ] 识别需要查看的具体文件和行数
- [ ] 规划工具调用顺序
- [ ] 设置输出长度预期

### 开发中检查
- [ ] 每次工具调用前评估必要性
- [ ] 使用最精确的参数
- [ ] 避免重复操作
- [ ] 监控输出长度

### 开发后检查
- [ ] 验证修改效果
- [ ] 清理临时文件
- [ ] 记录优化经验
- [ ] 更新最佳实践

## 自动化工具

使用提供的工具进行监控：

```bash
# 运行token优化器
node .trae/token-optimizer.js

# 查看优化报告
cat .trae/token-optimization-report.json
```

## 应急处理

当遇到token过长问题时：

1. **立即停止** 当前操作
2. **分析原因** 识别导致问题的具体操作
3. **重新规划** 使用更精确的方法
4. **分步执行** 将大任务分解为小步骤
5. **验证效果** 确保问题得到解决

## 长期维护

- 定期审查和更新优化策略
- 收集和分析token使用数据
- 持续改进工具和流程
- 培训团队成员使用最佳实践

---

**记住：预防胜于治疗。始终优先考虑精确性和效率，而非完整性和详尽性。**