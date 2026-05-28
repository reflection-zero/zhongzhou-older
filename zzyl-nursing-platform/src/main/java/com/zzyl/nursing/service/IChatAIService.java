package com.zzyl.nursing.service;

public interface IChatAIService
{
    /**
     * 生成 AI 回复
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param userType 用户类型 0=admin,1=family_member
     * @param userMessage 用户消息
     * @return AI 回复内容
     */
    String generateResponse(Long sessionId, Long userId, Integer userType, String userMessage);
}
