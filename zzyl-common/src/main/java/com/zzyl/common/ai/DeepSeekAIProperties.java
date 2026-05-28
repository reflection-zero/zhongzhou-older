package com.zzyl.common.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekAIProperties {

    private String apiKey = "";
    private String model = "deepseek-v4-flash";
    private Double temperature = 0.7;
    private Integer maxTokens = 4000;
}
