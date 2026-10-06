package com.icbc.online.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI生图请求
 */
@Data
public class AiImageRequest {

    @NotBlank(message = "图片描述不能为空")
    private String prompt;

    private String sessionId;

    /** 图片风格: realistic/cartoon/logo */
    private String style = "realistic";
}
