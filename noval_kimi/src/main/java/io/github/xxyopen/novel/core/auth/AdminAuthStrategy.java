package io.github.xxyopen.novel.core.auth;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.xxyopen.novel.core.common.constant.ErrorCodeEnum;
import io.github.xxyopen.novel.core.common.exception.BusinessException;
import io.github.xxyopen.novel.core.constant.SystemConfigConsts;
import io.github.xxyopen.novel.core.util.JwtUtils;
import io.github.xxyopen.novel.dao.entity.SysUser;
import io.github.xxyopen.novel.dao.entity.SysUserRole;
import io.github.xxyopen.novel.dao.mapper.SysUserMapper;
import io.github.xxyopen.novel.dao.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 平台后台管理系统 认证授权策略
 * 校验 JWT token 并确认用户是管理员角色
 *
 * @author xiongxiaoyang
 * @date 2022/5/18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAuthStrategy implements AuthStrategy {

    private final JwtUtils jwtUtils;

    private final SysUserMapper sysUserMapper;

    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public void auth(String token, String requestUri) throws BusinessException {
        // 1. 校验 Token 并解析用户ID (systemKey="admin")
        Long sysUserId = jwtUtils.parseToken(token, SystemConfigConsts.NOVEL_ADMIN_KEY);
        if (sysUserId == null) {
            throw new BusinessException(ErrorCodeEnum.USER_LOGIN_EXPIRED);
        }

        // 2. 查询管理员用户信息
        SysUser sysUser = sysUserMapper.selectById(sysUserId);
        if (sysUser == null) {
            throw new BusinessException(ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST);
        }
        if (sysUser.getStatus() == null || sysUser.getStatus() != 1) {
            throw new BusinessException(ErrorCodeEnum.USER_UN_AUTH);
        }

        // 3. 校验是否拥有管理员角色
        SysUserRole userRole = sysUserRoleMapper.selectOne(
            new QueryWrapper<SysUserRole>().eq("user_id", sysUserId)
        );
        if (userRole == null) {
            throw new BusinessException(ErrorCodeEnum.USER_UN_AUTH);
        }

        // 4. 将管理员ID放入上下文
        UserHolder.setUserId(sysUserId);
    }
}
