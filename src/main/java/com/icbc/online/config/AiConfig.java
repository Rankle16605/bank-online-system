package com.icbc.online.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * AI配置 - 显式创建ChatModel适配火山引擎Doubao大模型
 * 解决Spring AI自动配置与Doubao API兼容性问题
 * StreamingChatModel 使用Spring AI自动配置即可
 */
@Slf4j
@Configuration
public class AiConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model:doubao-seed-1-8-251228}")
    private String model;

    @Value("${spring.ai.openai.chat.options.temperature:0.7}")
    private Double temperature;

    @Value("${spring.ai.openai.chat.options.max-tokens:2000}")
    private Integer maxTokens;

    /**
     * 创建OpenAiApi实例，适配火山引擎Doubao兼容端点
     */
    @Bean
    @Primary
    public OpenAiApi openAiApi() {
        log.info("[AI配置] 初始化Doubao API: baseUrl={}, model={}", baseUrl, model);
        return new OpenAiApi(baseUrl, apiKey, RestClient.builder(), WebClient.builder());
    }

    /**
     * 普通对话模型 - 手动创建以确保正确配置
     */
    @Bean
    @Primary
    public ChatModel chatModel(OpenAiApi openAiApi) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(model)
                .withTemperature(temperature)
                .withMaxTokens(maxTokens)
                .build();
        OpenAiChatModel chatModel = new OpenAiChatModel(openAiApi, options);
        log.info("[AI配置] ChatModel初始化完成: model={}, temperature={}, maxTokens={}",
                model, temperature, maxTokens);
        return chatModel;
    }
}
