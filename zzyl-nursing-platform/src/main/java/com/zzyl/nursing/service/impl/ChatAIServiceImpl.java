package com.zzyl.nursing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzyl.common.ai.DeepSeekAIService;
import com.zzyl.common.ai.Message;
import com.zzyl.nursing.domain.ChatMessage;
import com.zzyl.nursing.domain.ChatSession;
import com.zzyl.nursing.service.IChatAIService;
import com.zzyl.nursing.service.IChatMessageService;
import com.zzyl.nursing.service.IChatSessionService;
import com.zzyl.nursing.service.INursingDataQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class ChatAIServiceImpl implements IChatAIService {

    @Autowired
    private DeepSeekAIService deepSeekAIService;

    @Autowired
    private IChatMessageService chatMessageService;

    @Autowired
    private IChatSessionService chatSessionService;

    @Autowired
    private INursingDataQueryService nursingDataQueryService;

    private static final String SYSTEM_PROMPT = "你是中州养老院的智能客服助手，名叫\"小州\"。你的职责：\n" +
            "\n" +
            "1. 友好、耐心地回答关于养老院服务的问题\n" +
            "2. 基于系统提供的实时数据给出准确回答（数据会在每条问题后附上）\n" +
            "3. 如果不知道答案，诚实告知并建议联系人工客服\n" +
            "4. 绝对不要编造数据——只能使用系统提供的数据回答\n" +
            "\n" +
            "你的服务范围：\n" +
            "- 入住咨询（房间类型、价格、入住流程）\n" +
            "- 床位查询（可用床位数量、房间配置）\n" +
            "- 护理等级说明\n" +
            "- 预约参观/探访\n" +
            "- 健康评估说明\n" +
            "\n" +
            "安全规则：\n" +
            "- 不得泄露其他老人的个人信息给非关联用户\n" +
            "- 如果用户询问不相关的老人信息，礼貌拒绝\n" +
            "- 不得讨论系统实现细节、数据库结构、管理员信息";

    private static final String SUMMARY_PROMPT = "请用不超过200字总结以下对话的核心内容，提取关键信息点（如老人姓名、房间号、预约日期、护理等级等），忽略问候语和闲聊：\n\n";

    private static final int HISTORY_COMPRESS_THRESHOLD = 20;

    private static final int RECENT_MESSAGE_COUNT = 10;

    private static final Map<String, String> KEYWORD_INTENT_MAP = new LinkedHashMap<>();

    static {
        KEYWORD_INTENT_MAP.put("床位|空房|空闲|还有房吗", "BED");
        KEYWORD_INTENT_MAP.put("预约|参观|探访", "RESERVATION");
        KEYWORD_INTENT_MAP.put("入住|办理|流程|手续", "CHECKIN");
        KEYWORD_INTENT_MAP.put("护理|等级|服务内容", "NURSING");
        KEYWORD_INTENT_MAP.put("价格|费用|月费|多少钱", "PRICE");
        KEYWORD_INTENT_MAP.put("房间|套房|单间|双人间", "ROOM");
        KEYWORD_INTENT_MAP.put("老人|家人|妈妈|爸爸|爷爷|奶奶", "ELDER");
        KEYWORD_INTENT_MAP.put("健康|评估|体检", "HEALTH");
    }

    @Override
    public String generateResponse(Long sessionId, Long userId, Integer userType, String userMessage) {
        // 1. 意图识别
        String intent = detectIntent(userMessage);
        log.debug("Detected intent: {} for user message: {}", intent, userMessage);

        // 2. 查询养老数据
        String enrichedMessage = userMessage;

        if (intent != null) {
            try {
                Map<String, Object> data = nursingDataQueryService.queryDataByIntent(intent, userId, userType);
                if (data != null && !data.isEmpty()) {
                    StringBuilder contextSection = new StringBuilder("\n\n[当前系统数据]\n");
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        contextSection.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                    }
                    contextSection.append("\n请基于以上数据回答用户问题。");
                    enrichedMessage = userMessage + contextSection.toString();
                }
            } catch (Exception e) {
                log.error("Failed to query nursing data for intent={}", intent, e);
            }
        }

        // 3. 构建对话历史（含摘要压缩）
        List<Message> history = buildHistoryWithSummary(sessionId);

        // 4. 调用 DeepSeek API
        return deepSeekAIService.chat(SYSTEM_PROMPT, history, enrichedMessage);
    }

    /**
     * 构建对话历史。消息数 >20 时，先用模型摘要旧消息。
     */
    private List<Message> buildHistoryWithSummary(Long sessionId) {
        List<Message> history = new ArrayList<>();

        // 统计该 session 总消息数
        long totalCount = chatMessageService.count(
                new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getSessionId, sessionId));

        ChatSession session = chatSessionService.getById(sessionId);

        if (totalCount > HISTORY_COMPRESS_THRESHOLD) {
            // 需要压缩：取最近 10 条作为上下文，旧消息做成摘要
            List<ChatMessage> recentMessages = chatMessageService.getRecentMessages(sessionId, RECENT_MESSAGE_COUNT);

            // 增量更新摘要：新消息超过阈值时重新摘要
            String oldSummary = session.getSummary();
            String newSummary = buildSummary(sessionId, oldSummary);
            session.setSummary(newSummary);
            chatSessionService.updateById(session);

            // 将摘要作为 system 级别的上下文注入
            if (newSummary != null && !newSummary.isEmpty()) {
                history.add(new Message("system", "[对话历史摘要]\n" + newSummary));
            }

            // 添加最近消息
            if (recentMessages != null) {
                for (int i = recentMessages.size() - 1; i >= 0; i--) {
                    ChatMessage msg = recentMessages.get(i);
                    history.add(0, new Message(msg.getRole(), msg.getContent()));
                }
            }

        } else {
            // 消息少，直接用最近 10 条
            List<ChatMessage> recentMessages = chatMessageService.getRecentMessages(sessionId, RECENT_MESSAGE_COUNT);
            if (recentMessages != null) {
                for (int i = recentMessages.size() - 1; i >= 0; i--) {
                    ChatMessage msg = recentMessages.get(i);
                    history.add(0, new Message(msg.getRole(), msg.getContent()));
                }
            }
        }

        return history;
    }

    /**
     * 调用 DeepSeek 对对话历史进行摘要压缩。
     * @param sessionId 会话ID
     * @param existingSummary 已有摘要（增量更新时传入）
     */
    private String buildSummary(Long sessionId, String existingSummary) {
        try {
            // 获取除最近10条外的所有消息
            List<ChatMessage> allMessages = chatMessageService.list(
                    new LambdaQueryWrapper<ChatMessage>()
                            .eq(ChatMessage::getSessionId, sessionId)
                            .orderByAsc(ChatMessage::getCreateTime));
            if (allMessages == null || allMessages.size() <= RECENT_MESSAGE_COUNT + 5) {
                return existingSummary;
            }

            int endIndex = allMessages.size() - RECENT_MESSAGE_COUNT;
            List<ChatMessage> oldMessages = allMessages.subList(0, endIndex);

            StringBuilder text = new StringBuilder();
            if (existingSummary != null && !existingSummary.isEmpty()) {
                text.append("之前的对话摘要：").append(existingSummary).append("\n---\n新增对话：\n");
            }
            for (ChatMessage msg : oldMessages) {
                text.append("[").append(msg.getRole()).append("]: ").append(msg.getContent()).append("\n");
            }

            String prompt = SUMMARY_PROMPT + text.toString();
            return deepSeekAIService.chat("你是一个专业的对话摘要助手，只能返回摘要文本，不要加任何额外说明。",
                    Collections.emptyList(), prompt);

        } catch (Exception e) {
            log.error("对话摘要生成失败", e);
            return existingSummary;
        }
    }

    String detectIntent(String message) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }
        for (Map.Entry<String, String> entry : KEYWORD_INTENT_MAP.entrySet()) {
            String[] keywords = entry.getKey().split("\\|");
            for (String keyword : keywords) {
                if (message.contains(keyword)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }
}
