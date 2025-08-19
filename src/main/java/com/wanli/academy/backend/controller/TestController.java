package com.wanli.academy.backend.controller;

import com.wanli.academy.backend.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 测试控制器 - 用于调试权限问题
 */
@RestController
@RequestMapping("/api/test")
public class TestController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    
    @Autowired
    private PermissionService permissionService;
    
    /**
     * 测试@PreAuthorize是否正常工作
     */
    @GetMapping("/permission/{assignmentId}")
    @PreAuthorize("@permissionService.canAccessAssignment(#assignmentId)")
    public ResponseEntity<String> testPermission(@PathVariable UUID assignmentId) {
        logger.info("TestController.testPermission called for assignmentId: {}", assignmentId);
        return ResponseEntity.ok("Permission test passed for assignment: " + assignmentId);
    }
    
    /**
     * 测试无权限控制的端点
     */
    @GetMapping("/no-permission")
    public ResponseEntity<String> testNoPermission() {
        logger.info("TestController.testNoPermission called");
        return ResponseEntity.ok("No permission test passed");
    }
}