package com.zzyl.nursing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzyl.nursing.domain.ChatSession;
import java.util.List;

public interface IChatSessionService extends IService<ChatSession>
{
    ChatSession createSession(Long userId, Integer userType, String title);

    List<ChatSession> listSessionsByUser(Long userId, Integer userType);

    ChatSession getById(Long sessionId);

    void closeSession(Long sessionId, Long userId, Integer userType);
}
