package com.zzyl.nursing.controller;

import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.common.utils.SecurityUtils;
import com.zzyl.nursing.domain.ChatMessage;
import com.zzyl.nursing.domain.ChatSession;
import com.zzyl.nursing.service.IChatMessageService;
import com.zzyl.nursing.service.IChatSessionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "管理端聊天")
@RestController
@RequestMapping("/nursing/chat")
public class AdminChatController extends BaseController
{
    @Autowired
    private IChatSessionService chatSessionService;

    @Autowired
    private IChatMessageService chatMessageService;

    @ApiOperation("获取当前管理员的会话列表")
    @PreAuthorize("@ss.hasPermi('nursing:chat:list')")
    @GetMapping("/sessions")
    public AjaxResult listSessions()
    {
        Long userId = SecurityUtils.getUserId();
        List<ChatSession> sessions = chatSessionService.listSessionsByUser(userId, 0);
        return success(sessions);
    }

    @ApiOperation("获取会话的消息列表")
    @PreAuthorize("@ss.hasPermi('nursing:chat:list')")
    @GetMapping("/sessions/{sessionId}")
    public AjaxResult getMessages(@PathVariable Long sessionId)
    {
        List<ChatMessage> messages = chatMessageService.listMessagesBySession(sessionId);
        return success(messages);
    }

    @ApiOperation("关闭会话")
    @PreAuthorize("@ss.hasPermi('nursing:chat:list')")
    @DeleteMapping("/sessions/{sessionId}")
    public AjaxResult closeSession(@PathVariable Long sessionId)
    {
        Long userId = SecurityUtils.getUserId();
        chatSessionService.closeSession(sessionId, userId, 0);
        return success();
    }
}
