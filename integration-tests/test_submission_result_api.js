const http = require('http');
const fs = require('fs');

let token;
try {
  token = fs.readFileSync('/tmp/test_token.txt', 'utf8').trim();
} catch(e) {
  console.log('Token file not found, will try to get token first');
}

if (!token) {
  // 先登录获取token
  const loginData = JSON.stringify({
    usernameOrEmail: 'test_student1',
    password: 'password123'
  });

  const loginReq = http.request({
    hostname: 'localhost',
    port: 8080,
    path: '/api/auth/login',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Content-Length': loginData.length
    }
  }, (res) => {
    let data = '';
    res.on('data', chunk => data += chunk);
    res.on('end', () => {
      try {
        const response = JSON.parse(data);
        if (response.data && response.data.accessToken) {
          token = response.data.accessToken;
          fs.writeFileSync('/tmp/test_token.txt', token);
          console.log('Login successful, token saved');
          testSubmissionResult();
        } else {
          console.log('Login failed:', data);
        }
      } catch(e) {
        console.log('Login response parse error:', e.message);
      }
    });
  });

  loginReq.on('error', (e) => {
    console.log('Login request error:', e.message);
  });

  loginReq.write(loginData);
  loginReq.end();
} else {
  testSubmissionResult();
}

function testSubmissionResult() {
  // 测试获取作业结果详情API
  const req = http.request({
    hostname: 'localhost',
    port: 8080,
    path: '/api/submissions/770e8400-e29b-41d4-a716-446655440004/result',
    method: 'GET',
    headers: {
      'Authorization': 'Bearer ' + token,
      'Content-Type': 'application/json'
    }
  }, (res) => {
    let data = '';
    res.on('data', chunk => data += chunk);
    res.on('end', () => {
      console.log('=== Submission Result API Test ===');
      console.log('Status:', res.statusCode);
      console.log('Response:', data);
      
      try {
        const parsed = JSON.parse(data);
        console.log('\n=== Parsed JSON ===');
        console.log(JSON.stringify(parsed, null, 2));
        
        // 检查数据结构
        console.log('\n=== Data Structure Analysis ===');
        if (parsed) {
          console.log('Has submissionId:', !!parsed.submissionId);
          console.log('Has totalScore:', !!parsed.totalScore);
          console.log('Has maxScore:', !!parsed.maxScore);
          console.log('Has teacherComment:', !!parsed.teacherComment);
          console.log('Has teacherFeedback:', !!parsed.teacherFeedback);
          console.log('Has questions array:', Array.isArray(parsed.questions));
          
          if (Array.isArray(parsed.questions) && parsed.questions.length > 0) {
            console.log('First question structure:');
            const firstQ = parsed.questions[0];
            console.log('  - questionId:', !!firstQ.questionId);
            console.log('  - content:', !!firstQ.content);
            console.log('  - studentAnswer:', !!firstQ.studentAnswer);
            console.log('  - standardAnswer:', !!firstQ.standardAnswer);
            console.log('  - score:', !!firstQ.score);
            console.log('  - maxScore:', !!firstQ.maxScore);
            console.log('  - explanation:', !!firstQ.explanation);
            console.log('  - videoUrl:', !!firstQ.videoUrl);
          }
        }
      } catch(e) {
        console.log('JSON parse error:', e.message);
      }
    });
  });

  req.on('error', (e) => {
    console.log('Request error:', e.message);
  });

  req.end();
}