package com.wanli.academy.backend.service;

import com.wanli.academy.backend.entity.User;
import com.wanli.academy.backend.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 自定义用户详情服务
 * 实现Spring Security的UserDetailsService接口
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 根据用户名加载用户详情
     * @param username 用户名或邮箱
     * @return UserDetails对象
     * @throws UsernameNotFoundException 用户不存在异常
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 尝试通过用户名或邮箱查找用户
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "用户不存在: " + username));

        // 检查用户是否激活
        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("用户账户已被禁用: " + username);
        }

        // 构建并返回UserDetails对象
        return buildUserDetails(user);
    }

    /**
     * 构建UserDetails对象
     * @param user 用户实体
     * @return UserDetails对象
     */
    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(getAuthorities(user))
                .accountExpired(false)
                .accountLocked(!user.getIsActive())
                .credentialsExpired(false)
                .disabled(!user.getIsActive())
                .build();
    }

    /**
     * 获取用户权限列表
     * @param user 用户实体
     * @return 权限集合
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> {
                    String roleName = role.getName();
                    // 如果角色名称已经包含ROLE_前缀，直接使用；否则添加前缀
                    if (roleName.startsWith("ROLE_")) {
                        return new SimpleGrantedAuthority(roleName);
                    } else {
                        return new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase());
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据用户ID加载用户详情
     * @param userId 用户ID
     * @return UserDetails对象
     * @throws UsernameNotFoundException 用户不存在异常
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "用户不存在，ID: " + userId));

        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("用户账户已被禁用，ID: " + userId);
        }

        return buildUserDetails(user);
    }
}