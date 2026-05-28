package com.zzyl.chat;

import com.zzyl.nursing.controller.AdminChatController;
import com.zzyl.nursing.domain.ChatSession;
import com.zzyl.nursing.domain.ChatMessage;
import com.zzyl.nursing.service.IChatSessionService;
import com.zzyl.nursing.service.IChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Admin Chat Controller 测试
 * 验证 REST 接口：
 * - GET  /nursing/chat/sessions      — 获取会话列表
 * - GET  /nursing/chat/sessions/{id} — 获取会话消息
 * - DELETE /nursing/chat/sessions/{id} — 关闭会话
 */
@DisplayName("聊天 REST Controller 测试")
@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private IChatSessionService chatSessionService;

    @Mock
    private IChatMessageService chatMessageService;

    @InjectMocks
    private AdminChatController controller;

    @Nested
    @DisplayName("获取会话列表")
    class ListSessions {

        @Test
        @DisplayName("应返回当前用户的会话列表")
        void shouldReturnSessionList() {
            ChatSession s1 = new ChatSession();
            s1.setId(1L);
            s1.setTitle("测试会话");
            s1.setUserId(1L);
            s1.setUserType(0);

            when(chatSessionService.listSessionsByUser(anyLong(), eq(0)))
                    .thenReturn(Arrays.asList(s1));

            var result = controller.listSessions();

            assertNotNull(result);
            verify(chatSessionService).listSessionsByUser(anyLong(), eq(0));
        }

        @Test
        @DisplayName("无会话时返回空列表")
        void shouldReturnEmptyList_whenNoSessions() {
            when(chatSessionService.listSessionsByUser(anyLong(), eq(0)))
                    .thenReturn(Collections.emptyList());

            var result = controller.listSessions();

            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("获取会话消息")
    class GetMessages {

        @Test
        @DisplayName("应返回指定会话的消息列表")
        void shouldReturnMessages() {
            ChatMessage msg1 = new ChatMessage();
            msg1.setId(1L);
            msg1.setSessionId(1L);
            msg1.setRole("user");
            msg1.setContent("你好");

            when(chatMessageService.listMessagesBySession(1L))
                    .thenReturn(Arrays.asList(msg1));

            var result = controller.getMessages(1L);

            assertNotNull(result);
            verify(chatMessageService).listMessagesBySession(1L);
        }
    }

    @Nested
    @DisplayName("关闭会话")
    class CloseSession {

        @Test
        @DisplayName("允许关闭自己的会话")
        void shouldCloseSession() {
            doNothing().when(chatSessionService).closeSession(1L, anyLong(), eq(0));

            var result = controller.closeSession(1L);

            assertNotNull(result);
            verify(chatSessionService).closeSession(eq(1L), anyLong(), eq(0));
        }
    }
}
