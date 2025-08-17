const bcrypt = require('bcrypt');

// 原始SQL文件中的哈希值
const originalHash = '$2b$10$aFPC5njXck.YuK8OafL6qOGQLgazzXwo011.jkf894HNqwT8fco/i';

// 测试脚本中使用的密码
const testPassword = 'password123';

console.log('验证原始SQL文件中的密码哈希...');
console.log('哈希值:', originalHash);
console.log('测试密码:', testPassword);

// 验证密码
bcrypt.compare(testPassword, originalHash, (err, result) => {
    if (err) {
        console.error('验证过程中出错:', err);
        return;
    }
    
    console.log(`\n密码 '${testPassword}' 与原始哈希值匹配:`, result);
    
    if (!result) {
        console.log('\n尝试其他可能的密码:');
        const possiblePasswords = ['admin123', 'password', '123456', 'test', 'admin'];
        
        possiblePasswords.forEach(pwd => {
            bcrypt.compare(pwd, originalHash, (err, match) => {
                if (err) return;
                if (match) {
                    console.log(`✅ 找到匹配密码: '${pwd}'`);
                }
            });
        });
        
        // 生成test123的正确哈希
        console.log('\n生成test123的正确哈希值:');
        bcrypt.hash(testPassword, 10, (err, hash) => {
            if (err) {
                console.error('生成哈希时出错:', err);
                return;
            }
            console.log('新的哈希值:', hash);
        });
    } else {
        console.log('✅ 密码验证成功！');
    }
});