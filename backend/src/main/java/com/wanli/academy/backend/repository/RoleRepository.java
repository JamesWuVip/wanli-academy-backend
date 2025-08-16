package com.wanli.academy.backend.repository;

import com.wanli.academy.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 角色数据访问接口
 * 继承JpaRepository提供基本的CRUD操作
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * 根据角色名称查找角色
     * @param name 角色名称
     * @return 角色对象的Optional包装
     */
    Optional<Role> findByName(String name);
    
    /**
     * 检查角色名称是否存在
     * @param name 角色名称
     * @return 是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 查找所有激活的角色
     * @return 激活角色列表
     */
    List<Role> findByIsActiveTrue();
    
    /**
     * 查找所有未激活的角色
     * @return 未激活角色列表
     */
    List<Role> findByIsActiveFalse();
    
    /**
     * 根据角色名称列表查找角色
     * @param names 角色名称列表
     * @return 角色列表
     */
    List<Role> findByNameIn(Set<String> names);
    
    /**
     * 根据用户ID查找该用户拥有的所有角色
     * @param userId 用户ID
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户名查找该用户拥有的所有角色
     * @param username 用户名
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.username = :username")
    List<Role> findByUsername(@Param("username") String username);
    
    /**
     * 根据描述模糊查询角色
     * @param description 描述关键词
     * @return 匹配的角色列表
     */
    List<Role> findByDescriptionContainingIgnoreCase(String description);
    
    /**
     * 根据角色名称模糊查询角色
     * @param name 角色名称关键词
     * @return 匹配的角色列表
     */
    List<Role> findByNameContainingIgnoreCase(String name);
    
    /**
     * 统计激活角色数量
     * @return 激活角色数量
     */
    long countByIsActiveTrue();
    
    /**
     * 统计总角色数量
     * @return 总角色数量
     */
    @Query("SELECT COUNT(r) FROM Role r")
    long countAllRoles();
    
    /**
     * 查找没有用户的角色
     * @return 没有用户的角色列表
     */
    @Query("SELECT r FROM Role r WHERE r.users IS EMPTY")
    List<Role> findRolesWithoutUsers();
    
    /**
     * 查找拥有用户数量最多的角色
     * @param limit 限制返回数量
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r ORDER BY SIZE(r.users) DESC")
    List<Role> findTopRolesByUserCount(@Param("limit") int limit);
}