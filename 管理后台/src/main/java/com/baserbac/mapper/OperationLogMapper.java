package com.baserbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baserbac.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<SysOperationLog> {
}
