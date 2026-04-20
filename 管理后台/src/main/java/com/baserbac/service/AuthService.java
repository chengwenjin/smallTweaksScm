package com.baserbac.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.baserbac.common.constant.RedisKeyConstant;
import com.baserbac.common.constant.CommonConstant;
import com.baserbac.common.enums.ResultCode;
import com.baserbac.common.exception.BusinessException;
import com.baserbac.common.util.IpUtil;
import com.baserbac.common.util.JwtUtil;
import com.baserbac.common.util.PasswordUtil;
import com.baserbac.dto.LoginDTO;
import com.baserbac.entity.*;
import com.baserbac.mapper.*;
import com.baserbac.vo.LoginVO;
import com.baserbac.vo.MenuVO;
import com.baserbac.vo.UserInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final LoginLogMapper loginLogMapper;
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final RoleApiMapper roleApiMapper;
    private final ApiPermissionMapper apiPermissionMapper;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 生成验证码
     */
    public Map<String, String> generateCaptcha() {
        // 生成验证码
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 100, 4, 50);
        
        // 生成唯一key
        String captchaKey = java.util.UUID.randomUUID().toString();
        
        // 存储到Redis，有效期5分钟
        String redisKey = RedisKeyConstant.CAPTCHA_PREFIX + captchaKey;
        redisTemplate.opsForValue().set(redisKey, captcha.getCode(), 5, TimeUnit.MINUTES);
        
        // 返回base64图片和key
        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", captchaKey);
        result.put("captchaImage", captcha.getImageBase64Data());
        
        return result;
    }

    /**
     * 用户登录
     */
    public LoginVO login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        String captcha = loginDTO.getCaptcha();
        String captchaKey = loginDTO.getCaptchaKey();

        // 1. 验证验证码
        validateCaptcha(captchaKey, captcha);

        // 2. 查询用户
        SysUser user = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
        );

        if (user == null) {
            recordLoginLog(username, 0, "用户不存在", getCurrentIp());
            throw new BusinessException(ResultCode.LOGIN_ERROR);
        }

        // 3. 检查账号是否被锁定
        if (user.getLockTime() != null && user.getLockTime().isAfter(LocalDateTime.now())) {
            recordLoginLog(username, 0, "账号已被锁定", getCurrentIp());
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
        }

        // 4. 检查账号状态
        if (user.getStatus() == 0) {
            recordLoginLog(username, 0, "账号已被禁用", getCurrentIp());
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 5. 验证密码
        if (!PasswordUtil.matches(password, user.getPassword())) {
            // 更新登录失败次数
            updateLoginFailCount(user);
            recordLoginLog(username, 0, "密码错误", getCurrentIp());
            throw new BusinessException(ResultCode.LOGIN_ERROR);
        }

        // 6. 登录成功，重置失败次数
        resetLoginFailCount(user);

        // 7. 生成Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 8. 存储Token到Redis
        saveTokenToRedis(user.getId(), accessToken, refreshToken);

        // 9. 更新最后登录信息
        updateLastLoginInfo(user.getId());

        // 10. 记录登录日志
        recordLoginLog(username, 1, null, getCurrentIp());

        // 11. 构建返回结果
        return LoginVO.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
            .userId(user.getId())
            .username(user.getUsername())
            .realName(user.getRealName())
            .build();
    }

    /**
     * 刷新Token
     */
    public LoginVO refreshToken(String refreshToken) {
        // 验证Refresh Token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);

        // 验证Redis中的Token
        String redisKey = RedisKeyConstant.REFRESH_TOKEN_PREFIX + userId;
        Object cachedToken = redisTemplate.opsForValue().get(redisKey);
        
        if (cachedToken == null || !cachedToken.equals(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        // 生成新的Token
        String newAccessToken = jwtUtil.generateAccessToken(userId, username);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);

        // 更新Redis
        saveTokenToRedis(userId, newAccessToken, newRefreshToken);

        // 查询用户信息
        SysUser user = userMapper.selectById(userId);

        return LoginVO.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
            .userId(userId)
            .username(username)
            .realName(user != null ? user.getRealName() : null)
            .build();
    }

    /**
     * 登出
     */
    public void logout(Long userId) {
        // 删除Redis中的Token
        redisTemplate.delete(RedisKeyConstant.TOKEN_PREFIX + userId);
        redisTemplate.delete(RedisKeyConstant.REFRESH_TOKEN_PREFIX + userId);
        
        log.info("用户 {} 已登出", userId);
    }

    /**
     * 获取用户信息（包含角色、菜单树、权限标识）
     */
    public Map<String, Object> getUserInfo(Long userId) {
        // 1. 查询用户基本信息
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 2. 查询用户角色
        List<SysUserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
        );
        
        List<Long> roleIds = userRoles.stream()
            .map(SysUserRole::getRoleId)
            .collect(Collectors.toList());
        
        List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
        List<String> roleNames = roles.stream()
            .map(SysRole::getRoleName)
            .collect(Collectors.toList());

        // 3. 判断是否为超级管理员
        boolean isSuperAdmin = roleIds.contains(CommonConstant.SUPER_ADMIN_ROLE_ID);

        // 4. 获取权限标识集合
        Set<String> permissions;
        if (isSuperAdmin) {
            // 超级管理员拥有所有权限
            List<SysApiPermission> allPerms = apiPermissionMapper.selectList(
                new LambdaQueryWrapper<SysApiPermission>()
                    .eq(SysApiPermission::getStatus, 1)
            );
            permissions = allPerms.stream()
                .map(SysApiPermission::getPermissionKey)
                .collect(Collectors.toSet());
        } else {
            // 普通用户：通过角色获取权限
            List<SysRoleApi> roleApis = roleApiMapper.selectList(
                new LambdaQueryWrapper<SysRoleApi>()
                    .in(SysRoleApi::getRoleId, roleIds)
            );
            
            List<Long> apiIds = roleApis.stream()
                .map(SysRoleApi::getApiId)
                .distinct()
                .collect(Collectors.toList());
            
            if (!apiIds.isEmpty()) {
                List<SysApiPermission> apiPerms = apiPermissionMapper.selectBatchIds(apiIds);
                permissions = apiPerms.stream()
                    .map(SysApiPermission::getPermissionKey)
                    .collect(Collectors.toSet());
            } else {
                permissions = new HashSet<>();
            }
        }

        // 5. 缓存权限到Redis
        String permsKey = RedisKeyConstant.PERMISSIONS_PREFIX + userId;
        redisTemplate.opsForSet().add(permsKey, permissions.toArray());
        redisTemplate.expire(permsKey, RedisKeyConstant.PERMISSIONS_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // 6. 获取菜单树
        List<MenuVO> menuTree;
        if (isSuperAdmin) {
            // 超级管理员获取所有启用的菜单
            menuTree = buildMenuTree(null, true);
        } else {
            // 普通用户：通过角色获取菜单
            List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                    .in(SysRoleMenu::getRoleId, roleIds)
            );
            
            List<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());
            
            menuTree = buildMenuTree(menuIds, false);
        }

        // 7. 缓存菜单到Redis
        String menusKey = RedisKeyConstant.MENUS_PREFIX + userId;
        redisTemplate.opsForValue().set(menusKey, menuTree, RedisKeyConstant.MENUS_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // 8. 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("user", UserInfoVO.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .realName(user.getRealName())
            .phone(user.getPhone())
            .email(user.getEmail())
            .avatar(user.getAvatar())
            .roles(roleNames)
            .permissions(new ArrayList<>(permissions))
            .build());
        result.put("menus", menuTree);
        result.put("permissions", new ArrayList<>(permissions));

        return result;
    }

    /**
     * 构建菜单树
     */
    private List<MenuVO> buildMenuTree(List<Long> menuIds, boolean isSuperAdmin) {
        // 查询菜单
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<SysMenu>()
            .eq(SysMenu::getStatus, 1)
            .orderByAsc(SysMenu::getSortOrder);
        
        if (!isSuperAdmin && menuIds != null && !menuIds.isEmpty()) {
            wrapper.in(SysMenu::getId, menuIds);
        }
        
        List<SysMenu> menus = menuMapper.selectList(wrapper);
        
        // 转换为VO
        List<MenuVO> menuVOs = menus.stream().map(menu -> {
            MenuVO vo = new MenuVO();
            vo.setId(menu.getId());
            vo.setParentId(menu.getParentId());
            vo.setMenuName(menu.getMenuName());
            vo.setMenuType(menu.getMenuType());
            vo.setPermissionKey(menu.getPermissionKey());
            vo.setPath(menu.getPath());
            vo.setComponent(menu.getComponent());
            vo.setIcon(menu.getIcon());
            vo.setSortOrder(menu.getSortOrder());
            vo.setIsVisible(menu.getIsVisible());
            vo.setIsCached(menu.getIsCached());
            vo.setIsExternal(menu.getIsExternal());
            vo.setStatus(menu.getStatus());
            vo.setCreateTime(menu.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
        
        // 构建树形结构
        return buildTree(menuVOs, 0L);
    }

    /**
     * 递归构建树形结构
     */
    private List<MenuVO> buildTree(List<MenuVO> allMenus, Long parentId) {
        return allMenus.stream()
            .filter(menu -> menu.getParentId().equals(parentId))
            .peek(menu -> menu.setChildren(buildTree(allMenus, menu.getId())))
            .collect(Collectors.toList());
    }

    /**
     * 验证验证码
     */
    private void validateCaptcha(String captchaKey, String captcha) {
        String redisKey = RedisKeyConstant.CAPTCHA_PREFIX + captchaKey;
        Object cachedCaptcha = redisTemplate.opsForValue().get(redisKey);
        
        if (cachedCaptcha == null) {
            throw new BusinessException(ResultCode.CAPTCHA_EXPIRED);
        }
        
        if (!cachedCaptcha.toString().equalsIgnoreCase(captcha)) {
            throw new BusinessException(ResultCode.CAPTCHA_ERROR);
        }
        
        // 验证成功后删除验证码
        redisTemplate.delete(redisKey);
    }

    /**
     * 更新登录失败次数
     */
    private void updateLoginFailCount(SysUser user) {
        int failCount = user.getLoginFailCount() == null ? 0 : user.getLoginFailCount();
        failCount++;
        
        user.setLoginFailCount(failCount);
        
        // 失败5次锁定30分钟
        if (failCount >= 5) {
            user.setLockTime(LocalDateTime.now().plusMinutes(30));
            log.warn("用户 {} 登录失败次数过多，账号已锁定30分钟", user.getUsername());
        }
        
        userMapper.updateById(user);
    }

    /**
     * 重置登录失败次数
     */
    private void resetLoginFailCount(SysUser user) {
        user.setLoginFailCount(0);
        user.setLockTime(null);
        userMapper.updateById(user);
    }

    /**
     * 保存Token到Redis
     */
    private void saveTokenToRedis(Long userId, String accessToken, String refreshToken) {
        // 保存Access Token，30分钟过期
        redisTemplate.opsForValue().set(
            RedisKeyConstant.TOKEN_PREFIX + userId,
            accessToken,
            RedisKeyConstant.TOKEN_EXPIRE_SECONDS,
            TimeUnit.SECONDS
        );
        
        // 保存Refresh Token，7天过期
        redisTemplate.opsForValue().set(
            RedisKeyConstant.REFRESH_TOKEN_PREFIX + userId,
            refreshToken,
            RedisKeyConstant.REFRESH_TOKEN_EXPIRE_SECONDS,
            TimeUnit.SECONDS
        );
    }

    /**
     * 更新最后登录信息
     */
    private void updateLastLoginInfo(Long userId) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(getCurrentIp());
        userMapper.updateById(user);
    }

    /**
     * 记录登录日志
     */
    private void recordLoginLog(String username, int status, String failReason, String ip) {
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUsername(username);
        loginLog.setLoginStatus(status);
        loginLog.setFailReason(failReason);
        loginLog.setIp(ip);
        loginLog.setLoginTime(LocalDateTime.now());
        
        loginLogMapper.insert(loginLog);
    }

    /**
     * 获取当前请求IP
     */
    private String getCurrentIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return IpUtil.getIpAddress(request);
            }
        } catch (Exception e) {
            log.error("获取IP失败", e);
        }
        return "unknown";
    }
}
