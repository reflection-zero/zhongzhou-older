package com.zzyl.nursing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.nursing.domain.ChatSession;
import com.zzyl.nursing.mapper.ChatSessionMapper;
import com.zzyl.nursing.service.IChatSessionService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements IChatSessionService
{
    @Override
    public ChatSession createSession(Long userId, Integer userType, String title) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setUserType(userType);
        session.setTitle(title != null && title.length() > 100 ? title.substring(0, 100) : title);
        session.setStatus(1);
        save(session);
        return session;
    }

    @Override
    public List<ChatSession> listSessionsByUser(Long userId, Integer userType) {
        return baseMapper.selectLatestSessionsByUser(userId, userType);
    }

    @Override
    public ChatSession getById(Long sessionId) {
        ChatSession session = super.getById(sessionId);
        if (session == null) {
            throw new ServiceException("会话不存在");
        }
        return session;
    }

    @Override
    public void closeSession(Long sessionId, Long userId, Integer userType) {
        ChatSession session = getById(sessionId);
        if (!session.getUserId().equals(userId) || !session.getUserType().equals(userType)) {
            throw new ServiceException("无权操作该会话");
        }
        session.setStatus(0);
        updateById(session);
    }
}
