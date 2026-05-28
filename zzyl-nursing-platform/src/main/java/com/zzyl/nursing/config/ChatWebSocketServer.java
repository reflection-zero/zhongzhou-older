package com.zzyl.nursing.config;

import cn.hutool.json.JSONUtil;
import com.zzyl.nursing.dto.ChatRequestDto;
import com.zzyl.nursing.vo.ChatMessageVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/chat/{userId}/{userType}")
public class ChatWebSocketServer {

    /** key = userType:userId */
    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 消息处理器，由 Spring 容器通过 ChatAsyncProcessor 设置 */
    private static ChatAsyncProcessor asyncProcessor;

    public static void setAsyncProcessor(ChatAsyncProcessor processor) {
        ChatWebSocketServer.asyncProcessor = processor;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId, @PathParam("userType") String userType) {
        String key = userType + ":" + userId;
        log.info("聊天客户端连接: {}", key);
        SESSION_MAP.put(key, session);
    }

    @OnMessage
    public void onMessage(Session session, String message,
                          @PathParam("userId") String userId,
                          @PathParam("userType") String userType) {
        try {
            ChatRequestDto dto = JSONUtil.toBean(message, ChatRequestDto.class);
            log.debug("收到聊天消息: userId={}, userType={}, dto={}", userId, userType, message);

            if (dto == null || dto.getContent() == null || dto.getContent().trim().isEmpty()) {
                sendError(session, null, "消息不能为空");
                return;
            }

            if (dto.getContent().length() > 500) {
                sendError(session, dto.getSessionId(), "消息长度不能超过500字");
                return;
            }

            if (asyncProcessor != null) {
                asyncProcessor.processMessage(userId, userType, dto);
            } else {
                log.error("ChatAsyncProcessor 未初始化");
                sendError(session, dto.getSessionId(), "系统暂未就绪，请稍后再试");
            }

        } catch (Exception e) {
            log.error("处理聊天消息失败", e);
            sendError(session, null, "消息处理失败");
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId, @PathParam("userType") String userType) {
        String key = userType + ":" + userId;
        log.info("聊天客户端断开: {}", key);
        SESSION_MAP.remove(key);
    }

    @OnError
    public void onError(Session session, Throwable throwable,
                        @PathParam("userId") String userId, @PathParam("userType") String userType) {
        String key = userType + ":" + userId;
        log.error("WebSocket 错误: {} - {}", key, throwable.getMessage());
        SESSION_MAP.remove(key);
    }

    /**
     * 发送消息给指定用户
     */
    public static void sendMessage(String userId, String userType, ChatMessageVo vo) {
        String key = userType + ":" + userId;
        Session session = SESSION_MAP.get(key);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(JSONUtil.toJsonStr(vo));
            } catch (IOException e) {
                log.error("推送聊天消息失败: {}", key, e);
            }
        }
    }

    private void sendError(Session session, Long sessionId, String errorContent) {
        ChatMessageVo vo = new ChatMessageVo();
        vo.setType("error");
        vo.setSessionId(sessionId);
        vo.setContent(errorContent);
        try {
            session.getBasicRemote().sendText(JSONUtil.toJsonStr(vo));
        } catch (IOException e) {
            log.error("发送错误消息失败", e);
        }
    }

    public static String now() {
        return LocalDateTime.now().format(FORMATTER);
    }
}
