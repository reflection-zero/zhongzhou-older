package com.zzyl.nursing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzyl.nursing.domain.ChatMessage;
import java.util.List;

public interface IChatMessageService extends IService<ChatMessage>
{
    ChatMessage saveMessage(Long sessionId, String role, String content, String contextData);

    List<ChatMessage> listMessagesBySession(Long sessionId);

    List<ChatMessage> getRecentMessages(Long sessionId, Integer limit);
}
