package com.zzyl.common.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzyl.common.exception.AIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class DeepSeekAIService {

    @Autowired
    private DeepSeekAIProperties properties;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";

    public DeepSeekAIService() {
        this.restTemplate = new RestTemplate();
        // 不直接用 SimpleClientHttpRequestFactory 的默认超时,设置合理的超时
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);  // 10s
        factory.setReadTimeout(60000);     // 60s - AI 响应可能比较慢
        this.restTemplate.setRequestFactory(factory);
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 调用 DeepSeek Chat Completion API
     *
     * @param systemPrompt 系统提示词
     * @param history 对话历史（最近N条），按时间从旧到新
     * @param userMessage 当前用户消息（已拼接好养老数据上下文）
     * @return AI 回复文本
     */
    public String chat(String systemPrompt, List<com.zzyl.common.ai.Message> history, String userMessage) {
        if (properties.getApiKey() == null || properties.getApiKey().isEmpty()
                || "your-api-key-here".equals(properties.getApiKey())) {
            throw new AIException("DeepSeek API Key 未配置，请在配置文件中设置 deepseek.apiKey");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(properties.getApiKey());

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));

            if (history != null) {
                for (com.zzyl.common.ai.Message msg : history) {
                    messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
                }
            }

            messages.add(Map.of("role", "user", "content", userMessage));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", properties.getModel());
            body.put("messages", messages);
            body.put("temperature", properties.getTemperature());
            body.put("max_tokens", properties.getMaxTokens());

            String json = objectMapper.writeValueAsString(body);
            log.debug("DeepSeek request: {}", json);

            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("DeepSeek API error: status={}, body={}", response.getStatusCode(), response.getBody());
                throw new AIException("AI 服务返回错误，请稍后再试");
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.get("choices");
            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                log.error("DeepSeek API response has no choices: {}", response.getBody());
                throw new AIException("AI 服务返回格式异常");
            }

            JsonNode message = choices.get(0).get("message");
            if (message == null) {
                throw new AIException("AI 服务返回格式异常");
            }

            String content = message.get("content").asText();
            log.debug("DeepSeek response content length: {}", content != null ? content.length() : 0);
            return content;

        } catch (AIException e) {
            throw e;
        } catch (Exception e) {
            log.error("DeepSeek API call failed", e);
            throw new AIException("AI 服务暂时不可用，请稍后再试");
        }
    }
}
