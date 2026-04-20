package com.baserbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baserbac.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRoleMapper extends BaseMapper<SysUserRole> {
}
