package com.zzyl.nursing.config;

import com.zzyl.nursing.domain.ChatSession;
import com.zzyl.nursing.dto.ChatRequestDto;
import com.zzyl.nursing.service.IChatAIService;
import com.zzyl.nursing.service.IChatMessageService;
import com.zzyl.nursing.service.IChatSessionService;
import com.zzyl.nursing.vo.ChatMessageVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class ChatAsyncProcessor {

    @Autowired
    private IChatSessionService chatSessionService;

    @Autowired
    private IChatMessageService chatMessageService;

    @Autowired
    private IChatAIService chatAIService;

    @PostConstruct
    public void init() {
        ChatWebSocketServer.setAsyncProcessor(this);
    }

    @Async("threadPoolTaskExecutor")
    public void processMessage(String userIdStr, String userTypeStr, ChatRequestDto dto) {
        Long userId = Long.valueOf(userIdStr);
        Integer userType = "admin".equals(userTypeStr) ? 0 : 1;

        try {
            // 1. 获取或创建会话
            Long sessionId = dto.getSessionId();
            if (sessionId == null || sessionId == 0) {
                String title = dto.getContent().length() > 50
                        ? dto.getContent().substring(0, 50) + "..."
                        : dto.getContent();
                ChatSession session = chatSessionService.createSession(userId, userType, title);
                sessionId = session.getId();

                // 通知客户端新会话ID
                ChatMessageVo sessionVo = new ChatMessageVo();
                sessionVo.setType("session");
                sessionVo.setSessionId(sessionId);
                ChatWebSocketServer.sendMessage(userIdStr, userTypeStr, sessionVo);
            }

            // 2. 发送 typing 指示
            ChatMessageVo typingVo = new ChatMessageVo();
            typingVo.setType("typing");
            typingVo.setSessionId(sessionId);
            typingVo.setStatus(true);
            ChatWebSocketServer.sendMessage(userIdStr, userTypeStr, typingVo);

            // 3. 保存用户消息
            chatMessageService.saveMessage(sessionId, "user", dto.getContent(), null);

            // 4. 调用 AI 生成回复
            String aiResponse = chatAIService.generateResponse(sessionId, userId, userType, dto.getContent());

            // 5. 保存 AI 回复
            chatMessageService.saveMessage(sessionId, "assistant", aiResponse, null);

            // 6. 推送 AI 回复给客户端
            ChatMessageVo replyVo = new ChatMessageVo();
            replyVo.setType("message");
            replyVo.setSessionId(sessionId);
            replyVo.setRole("assistant");
            replyVo.setContent(aiResponse);
            replyVo.setTimestamp(ChatWebSocketServer.now());
            ChatWebSocketServer.sendMessage(userIdStr, userTypeStr, replyVo);

        } catch (Exception e) {
            log.error("AI 处理失败: userId={}, userType={}", userIdStr, userTypeStr, e);

            ChatMessageVo errorVo = new ChatMessageVo();
            errorVo.setType("error");
            errorVo.setSessionId(dto.getSessionId());
            errorVo.setContent("抱歉，AI 服务暂不可用，请稍后再试");
            ChatWebSocketServer.sendMessage(userIdStr, userTypeStr, errorVo);
        }
    }
}
