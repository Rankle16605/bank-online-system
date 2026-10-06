package com.icbc.online.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.icbc.online.model.Bill;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账单Mapper
 */
@Mapper
public interface BillMapper extends BaseMapper<Bill> {
}
