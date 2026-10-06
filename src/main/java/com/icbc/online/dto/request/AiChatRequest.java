package com.icbc.online.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI对话请求
 */
@Data
public class AiChatRequest {

    @NotBlank(message = "消息内容不能为空")
    private String message;

    private String sessionId;
}
