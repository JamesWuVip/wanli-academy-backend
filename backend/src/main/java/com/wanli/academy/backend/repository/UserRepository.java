package com.wanli.academy.backend.repository;

import com.wanli.academy.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 * 继承JpaRepository提供基本的CRUD操作
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户对象的Optional包装
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     * @param email 邮箱地址
     * @return 用户对象的Optional包装
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根据用户名或邮箱查找用户
     * @param username 用户名
     * @param email 邮箱地址
     * @return 用户对象的Optional包装
     */
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     * @param email 邮箱地址
     * @return 是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 查找所有激活的用户
     * @return 激活用户列表
     */
    List<User> findByIsActiveTrue();
    
    /**
     * 查找所有未激活的用户
     * @return 未激活用户列表
     */
    List<User> findByIsActiveFalse();
    
    /**
     * 根据角色名称查找用户
     * @param roleName 角色名称
     * @return 拥有该角色的用户列表
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    /**
     * 根据姓名模糊查询用户
     * @param firstName 名字
     * @param lastName 姓氏
     * @return 匹配的用户列表
     */
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);
    
    /**
     * 根据手机号查找用户
     * @param phoneNumber 手机号
     * @return 用户对象的Optional包装
     */
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    /**
     * 统计激活用户数量
     * @return 激活用户数量
     */
    long countByIsActiveTrue();
    
    /**
     * 统计总用户数量
     * @return 总用户数量
     */
    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();
}