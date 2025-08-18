-- 为测试题目添加视频URL数据
-- 用于测试视频播放功能

-- 更新数学基础练习题目的视频URL
UPDATE questions SET 
    explanation = '这是一道基础加法运算题。首先将个位数相加：5+7=12，写2进1；然后将十位数相加：2+3+1=6，所以答案是62。',
    video_url = 'https://www.w3schools.com/html/mov_bbb.mp4'
WHERE id = '550e8400-e29b-41d4-a716-446655440201';

UPDATE questions SET 
    explanation = '这是一道基础乘法运算题。8×9可以通过九九乘法表得出，8×9=72。',
    video_url = 'https://www.w3schools.com/html/movie.mp4'
WHERE id = '550e8400-e29b-41d4-a716-446655440202';

UPDATE questions SET 
    explanation = '这是一道基础减法运算题。100-45，从个位开始：0-5不够减，向十位借1，变成10-5=5；十位：9-4=5，所以答案是55。',
    video_url = 'https://sample-videos.com/zip/10/mp4/SampleVideo_1280x720_1mb.mp4'
WHERE id = '550e8400-e29b-41d4-a716-446655440203';

-- 更新数学应用题的视频URL
UPDATE questions SET 
    explanation = '这是一道除法应用题。总数24个苹果，平均分给3个朋友，用除法计算：24÷3=8，每人分得8个苹果。',
    video_url = 'https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4'
WHERE id = '550e8400-e29b-41d4-a716-446655440204';

UPDATE questions SET 
    explanation = '这是一道除法应用题。总页数120页，每天看15页，用除法计算需要的天数：120÷15=8天。',
    video_url = 'https://file-examples.com/storage/fe68c1f7d4c2d1b8e5c1f7d/2017/10/file_example_MP4_480_1_5MG.mp4'
WHERE id = '550e8400-e29b-41d4-a716-446655440205';

-- 更新语文题目的视频URL
UPDATE questions SET 
    explanation = '分析人物性格需要从文章中的具体事例入手，观察人物的言行举止，总结其性格特点。',
    video_url = 'https://www.w3schools.com/html/mov_bbb.mp4'
WHERE id = '550e8400-e29b-41d4-a716-446655440206';

-- 更新英语题目的视频URL
UPDATE questions SET 
    explanation = 'Apple是苹果的英文单词，发音为/ˈæpəl/，是最基础的英语词汇之一。',
    video_url = 'https://www.w3schools.com/html/movie.mp4'
WHERE id = '550e8400-e29b-41d4-a716-446655440208';

UPDATE questions SET 
    explanation = 'Beautiful的正确拼写是b-e-a-u-t-i-f-u-l，意思是美丽的、漂亮的。',
    video_url = 'https://sample-videos.com/zip/10/mp4/SampleVideo_1280x720_1mb.mp4'
WHERE id = '550e8400-e29b-41d4-a716-446655440209';

-- 验证更新结果
SELECT 
    q.id,
    h.title as homework_title,
    q.question_type,
    CASE 
        WHEN q.explanation IS NOT NULL THEN '有解析' 
        ELSE '无解析' 
    END as has_explanation,
    CASE 
        WHEN q.video_url IS NOT NULL THEN '有视频' 
        ELSE '无视频' 
    END as has_video,
    q.video_url
FROM questions q
JOIN homeworks h ON q.homework_id = h.id
WHERE h.title LIKE 'Test_%'
ORDER BY h.title, q.order_index;

-- 统计视频URL数据
SELECT 
    COUNT(*) as total_questions,
    COUNT(video_url) as questions_with_video,
    COUNT(explanation) as questions_with_explanation
FROM questions q
JOIN homeworks h ON q.homework_id = h.id
WHERE h.title LIKE 'Test_%';