package com.wanli.academy.backend.config;

import com.wanli.academy.backend.entity.Role;
import com.wanli.academy.backend.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 在应用启动时初始化基础数据，包括角色等
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("开始初始化基础数据...");
        initializeRoles();
        logger.info("基础数据初始化完成");
    }
    
    /**
     * 初始化角色数据
     */
    private void initializeRoles() {
        logger.info("初始化角色数据...");
        
        // 创建基础角色
        createRoleIfNotExists("ROLE_ADMIN", "系统管理员，拥有所有权限");
        createRoleIfNotExists("ROLE_STUDENT", "学生用户，可以查看和提交作业");
        
        // Sprint 2 新增角色
        createRoleIfNotExists("ROLE_HQ_TEACHER", "总部教师，可以创建和管理作业");
        createRoleIfNotExists("ROLE_FRANCHISE_TEACHER", "加盟商教师，可以查看和批阅作业");
        
        logger.info("角色数据初始化完成");
    }
    
    /**
     * 如果角色不存在则创建
     * @param roleName 角色名称
     * @param description 角色描述
     */
    private void createRoleIfNotExists(String roleName, String description) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = new Role(roleName, description);
            roleRepository.save(role);
            logger.info("创建角色: {} - {}", roleName, description);
        } else {
            logger.debug("角色已存在: {}", roleName);
        }
    }
}