package com.baserbac.security;

import com.baserbac.entity.SysUser;
import com.baserbac.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户详情服务实现
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户
        SysUser sysUser = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
        );

        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 检查用户状态
        if (sysUser.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }

        // TODO: 后续从数据库查询用户的角色和权限
        // 暂时返回空权限列表
        List<GrantedAuthority> authorities = Collections.emptyList();

        return new LoginUser(
            sysUser.getId(),
            sysUser.getUsername(),
            sysUser.getPassword(),
            authorities
        );
    }
}
