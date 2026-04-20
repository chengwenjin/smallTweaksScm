package com.baserbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baserbac.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<SysUser> {
}
