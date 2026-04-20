package com.baserbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baserbac.entity.SysLoginLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginLogMapper extends BaseMapper<SysLoginLog> {
}
