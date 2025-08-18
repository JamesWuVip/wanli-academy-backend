const bcrypt = require('bcrypt');

// 从数据库获取的密码哈希
const storedHash = '$2b$10$aFPC5njXck.YuK8OafL6qOGQLgazzXwo011.jkf894HNqwT8fco/i';

// 测试密码
const testPassword = 'password123';

console.log('验证密码匹配性...');
console.log('测试密码:', testPassword);
console.log('存储哈希:', storedHash);

bcrypt.compare(testPassword, storedHash, (err, result) => {
  if (err) {
    console.error('验证过程中出错:', err);
    return;
  }
  
  console.log('密码匹配结果:', result);
  
  if (result) {
    console.log('✅ 密码匹配成功！');
  } else {
    console.log('❌ 密码不匹配！');
  }
});