package com.aidotnet.erp.iam.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.security.JwtProvider;
import com.aidotnet.erp.iam.domain.AuthToken;
import com.aidotnet.erp.iam.domain.Tenant;
import com.aidotnet.erp.iam.domain.TenantStatus;
import com.aidotnet.erp.iam.domain.UserAccount;
import com.aidotnet.erp.iam.infrastructure.IamStore;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("IAM认证服务测试")
class AuthServiceTest {

    private IamStore iamStore;
    private JwtProvider jwtProvider;
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        iamStore = mock(IamStore.class);
        jwtProvider = mock(JwtProvider.class);
        passwordEncoder = mock(org.springframework.security.crypto.password.PasswordEncoder.class);
        authService = new AuthService(iamStore, jwtProvider, passwordEncoder);
    }

    @Test
    @DisplayName("登录成功应返回AuthToken")
    void loginSuccess() {
        Instant now = Instant.now();
        Tenant tenant = new Tenant("T1", "TestTenant", "TEST", TenantStatus.ACTIVE, "standard", null, now, now);
        UserAccount user = new UserAccount("U1", "T1", null, "admin", "admin@test.com", null, "$2a$10$hash", true, "active", null, now, now);
        when(iamStore.findTenant("T1")).thenReturn(Optional.of(tenant));
        when(iamStore.findUser("T1", "admin")).thenReturn(Optional.of(user));
        when(iamStore.findUserPermissions("U1")).thenReturn(Set.of("iam:user:read"));
        when(passwordEncoder.matches("password", "$2a$10$hash")).thenReturn(true);
        when(jwtProvider.createToken(eq("T1"), eq("U1"), eq("admin"), any())).thenReturn("jwt-token");
        when(iamStore.saveToken(any())).thenAnswer(inv -> inv.getArgument(0));
        when(iamStore.saveUser(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthToken token = authService.login("T1", "admin", "password");
        assertNotNull(token);
        assertEquals("jwt-token", token.token());
        verify(iamStore).saveToken(any());
        verify(iamStore).saveUser(any());
    }

    @Test
    @DisplayName("租户不存在应抛出异常")
    void loginTenantNotFound() {
        when(iamStore.findTenant("T1")).thenReturn(Optional.empty());
        assertThrows(BizException.class, () -> authService.login("T1", "admin", "password"));
    }

    @Test
    @DisplayName("密码错误应抛出异常")
    void loginWrongPassword() {
        Instant now = Instant.now();
        Tenant tenant = new Tenant("T1", "TestTenant", "TEST", TenantStatus.ACTIVE, "standard", null, now, now);
        UserAccount user = new UserAccount("U1", "T1", null, "admin", "admin@test.com", null, "$2a$10$hash", true, "active", null, now, now);
        when(iamStore.findTenant("T1")).thenReturn(Optional.of(tenant));
        when(iamStore.findUser("T1", "admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "$2a$10$hash")).thenReturn(false);
        assertThrows(BizException.class, () -> authService.login("T1", "admin", "wrong"));
    }
}
