package com.zzyl.chat;

import com.zzyl.nursing.service.impl.ChatAIServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatAIService 意图识别与摘要逻辑单元测试
 * 不依赖 Spring 容器，直接测试核心逻辑
 */
@DisplayName("ChatAI 服务单元测试")
class ChatAIServiceTest {

    private ChatAIServiceImpl chatAIService;

    @BeforeEach
    void setUp() {
        chatAIService = new ChatAIServiceImpl();
        // 不注入 Spring Bean，仅测试纯逻辑方法
    }

    // === 意图识别测试 ===

    @Test
    @DisplayName("床位关键词应识别为 BED")
    void detectIntent_shouldReturnBED_whenBedKeywordsPresent() {
        assertEquals("BED", invokeDetectIntent("现在还有空床位吗？"));
        assertEquals("BED", invokeDetectIntent("有哪些空闲的房间"));
        assertEquals("BED", invokeDetectIntent("还有房吗"));
    }

    @Test
    @DisplayName("预约关键词应识别为 RESERVATION")
    void detectIntent_shouldReturnRESERVATION_whenReservationKeywordsPresent() {
        assertEquals("RESERVATION", invokeDetectIntent("我想预约参观"));
        assertEquals("RESERVATION", invokeDetectIntent("可以探访吗"));
        assertEquals("RESERVATION", invokeDetectIntent("怎么预约"));
    }

    @Test
    @DisplayName("入住关键词应识别为 CHECKIN")
    void detectIntent_shouldReturnCHECKIN_whenCheckinKeywordsPresent() {
        assertEquals("CHECKIN", invokeDetectIntent("入住流程是怎样的"));
        assertEquals("CHECKIN", invokeDetectIntent("怎么办理手续"));
    }

    @Test
    @DisplayName("护理关键词应识别为 NURSING")
    void detectIntent_shouldReturnNURSING_whenNursingKeywordsPresent() {
        assertEquals("NURSING", invokeDetectIntent("有哪些护理等级"));
        assertEquals("NURSING", invokeDetectIntent("服务内容是什么"));
    }

    @Test
    @DisplayName("价格关键词应识别为 PRICE")
    void detectIntent_shouldReturnPRICE_whenPriceKeywordsPresent() {
        assertEquals("PRICE", invokeDetectIntent("月费多少钱"));
        assertEquals("PRICE", invokeDetectIntent("房间价格是多少"));
    }

    @Test
    @DisplayName("房间关键词应识别为 ROOM")
    void detectIntent_shouldReturnROOM_whenRoomKeywordsPresent() {
        assertEquals("ROOM", invokeDetectIntent("有单间吗"));
        assertEquals("ROOM", invokeDetectIntent("双人间和套房"));
    }

    @Test
    @DisplayName("老人关键词应识别为 ELDER")
    void detectIntent_shouldReturnELDER_whenElderKeywordsPresent() {
        assertEquals("ELDER", invokeDetectIntent("我妈妈住哪个房间"));
        assertEquals("ELDER", invokeDetectIntent("爷爷的信息"));
        assertEquals("ELDER", invokeDetectIntent("帮我查一下家人"));
    }

    @Test
    @DisplayName("健康关键词应识别为 HEALTH")
    void detectIntent_shouldReturnHEALTH_whenHealthKeywordsPresent() {
        assertEquals("HEALTH", invokeDetectIntent("健康评估结果"));
        assertEquals("HEALTH", invokeDetectIntent("体检报告"));
    }

    @Test
    @DisplayName("无关键词应返回 null")
    void detectIntent_shouldReturnNull_whenNoKeywordsMatch() {
        assertNull(invokeDetectIntent("你好"));
        assertNull(invokeDetectIntent("今天天气怎么样"));
        assertNull(invokeDetectIntent(""));
        assertNull(invokeDetectIntent(null));
    }

    @Test
    @DisplayName("第一个匹配的关键词应优先返回")
    void detectIntent_shouldReturnFirstMatch_whenMultipleIntentsPresent() {
        // "床位"在"房间"之前，应返回 BED
        assertEquals("BED", invokeDetectIntent("单间床位多少钱"));
    }

    // === 反射调用私有方法 ===

    private String invokeDetectIntent(String message) {
        try {
            java.lang.reflect.Method method = ChatAIServiceImpl.class.getDeclaredMethod("detectIntent", String.class);
            method.setAccessible(true);
            return (String) method.invoke(chatAIService, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
