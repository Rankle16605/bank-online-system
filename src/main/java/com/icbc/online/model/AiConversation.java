package com.icbc.online.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI对话记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_ai_conversation")
public class AiConversation {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("session_id")
    private String sessionId;

    @TableField("message_type")
    private Integer messageType;

    private String content;

    private String context;

    @TableField("response_time")
    private Integer responseTime;

    /** 图片URL (消息类型为3时有效) */
    @TableField("image_url")
    private String imageUrl;

    @TableField("created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
