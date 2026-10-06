package com.icbc.online.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.icbc.online.model.Account;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账户Mapper
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}
