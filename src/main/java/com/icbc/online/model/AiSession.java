package com.icbc.online.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI会话实体 - 管理用户与"小工"客服的会话
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_ai_session")
public class AiSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("session_id")
    private String sessionId;

    /** 会话标题(取首条消息前20字) */
    private String title;

    /** 消息数量 */
    @TableField("message_count")
    @Builder.Default
    private Integer messageCount = 0;

    /** 状态: 1=活跃, 0=已删除 */
    @Builder.Default
    private Integer status = 1;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
