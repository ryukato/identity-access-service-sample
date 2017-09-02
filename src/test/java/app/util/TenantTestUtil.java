package app.util;

import app.domain.ApiKeyInformation;
import app.domain.LoginCredential;
import app.domain.Tenant;
import app.domain.UserProfile;
import app.repository.TenantApiKeyRepository;
import app.repository.TenantRepository;

import java.time.LocalDateTime;

public class TenantTestUtil {
    private TenantTestUtil() {
    }
    public static Tenant createTestTenant() {
        return createTestTenant("test_tenant");
    }

    public static Tenant createTestTenant(String tenantAccount) {
        return createTestTenant(tenantAccount, "test1234@test.com");
    }

    public static Tenant createTestTenant(String tenantAccount, String email) {
        LoginCredential loginCredential = new LoginCredential();
        loginCredential.setAccount(tenantAccount);
        loginCredential.setPassword("test");
        return createTestTenant(email, "test tenant company", loginCredential);
    }

    public static Tenant createTestTenant(String email, LoginCredential loginCredential) {
        return createTestTenant(email, "test tenant company", loginCredential);
    }

    public static Tenant createTestTenant(String email, String companyName, LoginCredential loginCredential) {
        Tenant tenant = new Tenant();
        tenant.setCompanyName(companyName);
        tenant.setEmail(email);
        tenant.setLoginCredential(loginCredential);
        UserProfile profile = new UserProfile();
        tenant.setProfile(profile);
        return tenant;
    }

    public static Tenant prepareTestTenants(String tenantName, TenantRepository tenantRepository, TenantApiKeyRepository tenantApiKeyRepository, LocalDateTime testExpireDate) {
        Tenant tenant = TenantTestUtil.createTestTenant(tenantName);
        tenant = tenantRepository.save(tenant);
        ApiKeyInformation apiKeyInformation = new ApiKeyInformation(tenant.getId(), testExpireDate);
        tenant.setApiKeyInformation(apiKeyInformation);
        apiKeyInformation.setOwner(tenant);
        tenantApiKeyRepository.save(apiKeyInformation);
        return tenantRepository.save(tenant);
    }
}
