package com.zzyl.nursing.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzyl.nursing.domain.ChatMessage;
import com.zzyl.nursing.mapper.ChatMessageMapper;
import com.zzyl.nursing.service.IChatMessageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService
{
    @Override
    public ChatMessage saveMessage(Long sessionId, String role, String content, String contextData) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setContextData(contextData);
        save(message);
        return message;
    }

    @Override
    public List<ChatMessage> listMessagesBySession(Long sessionId) {
        return baseMapper.selectMessagesBySessionId(sessionId);
    }

    @Override
    public List<ChatMessage> getRecentMessages(Long sessionId, Integer limit) {
        return baseMapper.selectRecentMessages(sessionId, limit);
    }
}
