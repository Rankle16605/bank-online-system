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
 * 操作日志实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private String username;

    private String operation;

    private String method;

    private String params;

    @TableField("ip_address")
    private String ipAddress;

    private Integer status;

    @TableField("error_message")
    private String errorMessage;

    @TableField("operation_time")
    @Builder.Default
    private LocalDateTime operationTime = LocalDateTime.now();
}
