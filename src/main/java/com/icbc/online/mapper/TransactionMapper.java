package com.icbc.online.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.icbc.online.model.Transaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交易Mapper
 */
@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {
}
