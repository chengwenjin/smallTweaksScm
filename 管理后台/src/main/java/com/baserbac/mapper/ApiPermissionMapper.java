package com.baserbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baserbac.entity.SysApiPermission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApiPermissionMapper extends BaseMapper<SysApiPermission> {
}
