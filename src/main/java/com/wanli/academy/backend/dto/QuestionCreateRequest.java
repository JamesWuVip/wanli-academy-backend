package com.wanli.academy.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * 创建题目请求DTO
 */
@Schema(description = "创建题目请求")
public class QuestionCreateRequest {
    
    @Schema(description = "题目内容", example = "计算 2 + 3 = ?", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "题目内容不能为空")
    @Size(min = 1, max = 2000, message = "题目内容长度必须在1-2000个字符之间")
    private String content;
    
    @Schema(description = "题目类型", example = "选择题", allowableValues = {"选择题", "填空题", "简答题", "计算题"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "题目类型不能为空")
    @Size(max = 50, message = "题目类型长度不能超过50个字符")
    private String questionType;
    
    @Schema(description = "标准答案", example = "5")
    @Size(max = 2000, message = "标准答案长度不能超过2000个字符")
    private String standardAnswer;
    
    @Schema(description = "题目在作业中的顺序", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "题目顺序不能为空")
    @Min(value = 1, message = "题目顺序必须大于0")
    private Integer orderIndex;
    
    // 默认构造函数
    public QuestionCreateRequest() {}
    
    // 构造函数
    public QuestionCreateRequest(String content, String questionType, String standardAnswer, Integer orderIndex) {
        this.content = content;
        this.questionType = questionType;
        this.standardAnswer = standardAnswer;
        this.orderIndex = orderIndex;
    }
    
    // Getter和Setter方法
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getQuestionType() {
        return questionType;
    }
    
    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }
    
    public String getStandardAnswer() {
        return standardAnswer;
    }
    
    public void setStandardAnswer(String standardAnswer) {
        this.standardAnswer = standardAnswer;
    }
    
    public Integer getOrderIndex() {
        return orderIndex;
    }
    
    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }
    
    @Override
    public String toString() {
        return "QuestionCreateRequest{" +
                "content='" + content + '\'' +
                ", questionType='" + questionType + '\'' +
                ", standardAnswer='" + standardAnswer + '\'' +
                ", orderIndex=" + orderIndex +
                '}';
    }
}