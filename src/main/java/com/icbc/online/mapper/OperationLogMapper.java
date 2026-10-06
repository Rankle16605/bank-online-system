package com.icbc.online.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.icbc.online.model.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
