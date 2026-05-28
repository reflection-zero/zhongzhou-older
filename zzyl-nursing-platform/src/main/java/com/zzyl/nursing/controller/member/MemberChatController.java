package com.zzyl.nursing.controller.member;

import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.common.utils.UserThreadLocal;
import com.zzyl.nursing.domain.ChatMessage;
import com.zzyl.nursing.domain.ChatSession;
import com.zzyl.nursing.service.IChatMessageService;
import com.zzyl.nursing.service.IChatSessionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "家属端聊天")
@RestController
@RequestMapping("/member/chat")
public class MemberChatController extends BaseController
{
    @Autowired
    private IChatSessionService chatSessionService;

    @Autowired
    private IChatMessageService chatMessageService;

    @ApiOperation("获取当前家属的会话列表")
    @GetMapping("/sessions")
    public AjaxResult listSessions()
    {
        Long userId = UserThreadLocal.getUserId();
        List<ChatSession> sessions = chatSessionService.listSessionsByUser(userId, 1);
        return success(sessions);
    }

    @ApiOperation("获取会话的消息列表")
    @GetMapping("/sessions/{sessionId}")
    public AjaxResult getMessages(@PathVariable Long sessionId)
    {
        List<ChatMessage> messages = chatMessageService.listMessagesBySession(sessionId);
        return success(messages);
    }

    @ApiOperation("关闭会话")
    @DeleteMapping("/sessions/{sessionId}")
    public AjaxResult closeSession(@PathVariable Long sessionId)
    {
        Long userId = UserThreadLocal.getUserId();
        chatSessionService.closeSession(sessionId, userId, 1);
        return success();
    }
}
