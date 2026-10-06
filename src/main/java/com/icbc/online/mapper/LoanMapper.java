package com.icbc.online.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.icbc.online.model.Loan;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoanMapper extends BaseMapper<Loan> {
}
