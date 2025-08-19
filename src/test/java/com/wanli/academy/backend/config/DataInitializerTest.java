package com.wanli.academy.backend.config;

import com.wanli.academy.backend.entity.Role;
import com.wanli.academy.backend.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * DataInitializer测试类
 */
@ExtendWith(MockitoExtension.class)
class DataInitializerTest {
    
    @Mock
    private RoleRepository roleRepository;
    
    @InjectMocks
    private DataInitializer dataInitializer;
    
    @Test
    void shouldInitializeAllRoles_whenNoRolesExist() throws Exception {
        // Given
        when(roleRepository.existsByName(anyString())).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(new Role());
        
        // When
        dataInitializer.run();
        
        // Then
        verify(roleRepository, times(4)).existsByName(anyString());
        verify(roleRepository, times(4)).save(any(Role.class));
        
        // 验证创建了所有必需的角色
        verify(roleRepository).existsByName("ROLE_ADMIN");
        verify(roleRepository).existsByName("ROLE_STUDENT");
        verify(roleRepository).existsByName("ROLE_HQ_TEACHER");
        verify(roleRepository).existsByName("ROLE_FRANCHISE_TEACHER");
    }
    
    @Test
    void shouldNotCreateAnyRoles_whenAllRolesAlreadyExist() throws Exception {
        // Given
        when(roleRepository.existsByName(anyString())).thenReturn(true);
        
        // When
        dataInitializer.run();
        
        // Then
        verify(roleRepository, times(4)).existsByName(anyString());
        verify(roleRepository, never()).save(any(Role.class));
    }
    
    @Test
    void shouldCreateOnlyMissingRoles_whenSomeRolesExist() throws Exception {
        // Given
        when(roleRepository.existsByName("ROLE_ADMIN")).thenReturn(true);
        when(roleRepository.existsByName("ROLE_STUDENT")).thenReturn(true);
        when(roleRepository.existsByName("ROLE_HQ_TEACHER")).thenReturn(false);
        when(roleRepository.existsByName("ROLE_FRANCHISE_TEACHER")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(new Role());
        
        // When
        dataInitializer.run();
        
        // Then
        verify(roleRepository, times(4)).existsByName(anyString());
        verify(roleRepository, times(2)).save(any(Role.class));
    }
}