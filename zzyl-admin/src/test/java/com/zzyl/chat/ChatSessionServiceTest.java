package com.zzyl.chat;

import com.zzyl.common.exception.ServiceException;
import com.zzyl.nursing.domain.ChatSession;
import com.zzyl.nursing.mapper.ChatSessionMapper;
import com.zzyl.nursing.service.impl.ChatSessionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("聊天会话服务测试")
@ExtendWith(MockitoExtension.class)
class ChatSessionServiceTest {

    @Mock
    private ChatSessionMapper chatSessionMapper;

    @InjectMocks
    private ChatSessionServiceImpl service;

    @Nested
    @DisplayName("创建会话")
    class CreateSession {

        @Test
        @DisplayName("应正确创建会话并设置字段")
        void shouldCreateSessionWithCorrectFields() {
            ArgumentCaptor<ChatSession> captor = ArgumentCaptor.forClass(ChatSession.class);
            when(chatSessionMapper.insert(captor.capture())).thenReturn(1);

            service.createSession(1L, 0, "测试会话标题");

            ChatSession saved = captor.getValue();
            assertEquals(1L, saved.getUserId());
            assertEquals(0, saved.getUserType());
            assertEquals("测试会话标题", saved.getTitle());
            assertEquals(1, saved.getStatus());
        }

        @Test
        @DisplayName("标题超过100字符应截断")
        void shouldTruncateTitle_whenExceeds100Chars() {
            ArgumentCaptor<ChatSession> captor = ArgumentCaptor.forClass(ChatSession.class);
            when(chatSessionMapper.insert(captor.capture())).thenReturn(1);

            String longTitle = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaX";
            service.createSession(1L, 0, longTitle);

            ChatSession saved = captor.getValue();
            assertTrue(saved.getTitle().length() <= 100);
        }
    }

    @Nested
    @DisplayName("关闭会话 — 权限校验")
    class CloseSession {

        @Test
        @DisplayName("会话所属用户与操作者一致时允许关闭")
        void shouldAllowClose_whenSameUser() {
            ChatSession session = new ChatSession();
            session.setId(1L);
            session.setUserId(100L);
            session.setUserType(1);
            session.setStatus(1);

            when(chatSessionMapper.selectById(1L)).thenReturn(session);

            service.closeSession(1L, 100L, 1);

            verify(chatSessionMapper).updateById(argThat(s -> s.getStatus() == 0));
        }

        @Test
        @DisplayName("会话不属于当前用户时拒绝关闭")
        void shouldRejectClose_whenDifferentUser() {
            ChatSession session = new ChatSession();
            session.setId(1L);
            session.setUserId(100L);
            session.setUserType(1);

            when(chatSessionMapper.selectById(1L)).thenReturn(session);

            assertThrows(ServiceException.class, () ->
                    service.closeSession(1L, 999L, 1),
                    "无权操作该会话"
            );
        }

        @Test
        @DisplayName("userType 不匹配时拒绝关闭")
        void shouldRejectClose_whenDifferentUserType() {
            ChatSession session = new ChatSession();
            session.setId(1L);
            session.setUserId(100L);
            session.setUserType(1);

            when(chatSessionMapper.selectById(1L)).thenReturn(session);

            assertThrows(ServiceException.class, () ->
                    service.closeSession(1L, 100L, 0),
                    "无权操作该会话"
            );
        }

        @Test
        @DisplayName("会话不存在时抛出异常")
        void shouldThrow_whenSessionNotFound() {
            when(chatSessionMapper.selectById(999L)).thenReturn(null);

            assertThrows(ServiceException.class, () ->
                    service.closeSession(999L, 1L, 0)
            );
        }
    }

    @Nested
    @DisplayName("查询会话列表")
    class ListSessions {

        @Test
        @DisplayName("仅返回当前用户的会话")
        void shouldReturnOnlyCurrentUserSessions() {
            List<ChatSession> mockList = Collections.emptyList();
            when(chatSessionMapper.selectLatestSessionsByUser(100L, 1)).thenReturn(mockList);

            List<ChatSession> result = service.listSessionsByUser(100L, 1);

            assertNotNull(result);
            verify(chatSessionMapper).selectLatestSessionsByUser(100L, 1);
        }
    }
}
