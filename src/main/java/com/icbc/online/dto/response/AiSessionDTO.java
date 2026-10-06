package com.icbc.online.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI会话列表DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSessionDTO {
    private String sessionId;
    private String title;
    private Integer messageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
