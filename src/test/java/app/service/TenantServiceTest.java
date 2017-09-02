package app.service;

import app.domain.ApiKeyInformation;
import app.domain.Application;
import app.domain.PasswordUpdateRequest;
import app.domain.Tenant;
import app.error.DuplicatedEmailUserFoundException;
import app.error.NoUserLoginCredentialException;
import app.error.RecordNotFoundException;
import app.error.SameUserNameFoundException;
import app.repository.TenantApiKeyRepository;
import app.repository.TenantRepository;
import app.resource.EndUserResourceTestUtil;
import app.util.ApiKeyGenerator;
import app.util.ClientDetailsFactory;
import app.util.TenantTestUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TenantServiceTest {
    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantApiKeyRepository tenantApiKeyRepository;

    @Mock
    ClientDetailsFactory<Tenant> tenantClientDetailsFactory;

    @Mock
    private JdbcClientDetailsService jdbcClientDetailsService;

    @Mock
    private ClientDetailsFactory clientDetailsFactory;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private TenantService tenantService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Before
    public void setUp() {
        this.tenantService = new TenantService(tenantRepository,
                tenantApiKeyRepository,
                tenantClientDetailsFactory,
                jdbcClientDetailsService,
                passwordEncoder);
    }

    @Test
    public void createTenant() {
        Tenant tenant = createTestTenant();
        ApiKeyInformation apiKeyInformation = ApiKeyGenerator.generate(32);
        when(tenantRepository.save(tenant)).thenReturn(tenant);
        when(tenantApiKeyRepository.save(apiKeyInformation)).thenReturn(apiKeyInformation);

        tenant = tenantService.createTenant(tenant);
        assertEquals(tenant.getStatus(), Tenant.Status.CREATED);
        verify(tenantRepository, times(1)).save(tenant);
        assertNotNull(tenant.getApiKeyInformation());
        assertNotNull(tenant.getApiKeyInformation().getApiKey());
    }

    @Test
    public void createTenant_with_duplicated_tenant_account() {
        Tenant tenant = createTestTenant();
        when(tenantRepository.findByLoginCredentialAccount(tenant.getLoginCredential().getAccount())).thenReturn(tenant);

        try {
            tenantService.createTenant(tenant);
            fail();
        }catch (SameUserNameFoundException t) {
            // nothing to do
        }
    }

    @Test
    public void createTenant_with_duplicated_tenant_email() {
        Tenant tenant = createTestTenant();
        when(tenantRepository.findByEmail(tenant.getEmail())).thenReturn(tenant);

        try {
            tenantService.createTenant(tenant);
            fail();
        }catch (DuplicatedEmailUserFoundException t) {
            // nothing to do
        }
    }

    @Test
    public void createTenant_without_login_credential() {
        Tenant tenant = createTestTenant();
        tenant.setLoginCredential(null);

        try {
            tenantService.createTenant(tenant);
            fail();
        }catch (NoUserLoginCredentialException e){}
    }

    @Test
    public void createTenant_without_login_credential_account() {
        Tenant tenant = createTestTenant();
        tenant.getLoginCredential().setAccount(null);

        try {
            tenantService.createTenant(tenant);
            fail();
        }catch (NoUserLoginCredentialException e){}
    }

    @Test
    public void createTenant_without_login_credential_password() {
        Tenant tenant = createTestTenant();
        tenant.getLoginCredential().setPassword(null);

        try {
            tenantService.createTenant(tenant);
            fail();
        }catch (NoUserLoginCredentialException e){}
    }

    @Test
    public void updateTenant() {
        Tenant tenant = createTestTenant();
        Tenant updateTenant = createTestTenant();
        updateTenant.setEmail("updated_email@test.com");
        tenant.setId(UUID.randomUUID().toString());
        when(tenantRepository.findById(tenant.getId())).thenReturn(tenant);
        when(tenantRepository.save(updateTenant)).thenReturn(updateTenant);


        updateTenant = tenantService.update(tenant.getId(), updateTenant);
        verify(tenantRepository, times(1)).findById(tenant.getId());
        verify(tenantRepository, times(1)).save(updateTenant);
    }

    @Test
    public void updateTenantPassword() {
        Tenant tenant = createTestTenant();
        String currentPassword = tenant.getLoginCredential().getPassword();
        tenant.getLoginCredential().setPassword(passwordEncoder.encode(tenant.getLoginCredential().getPassword()));
        tenant.setId(UUID.randomUUID().toString());
        when(tenantRepository.findById(tenant.getId())).thenReturn(tenant);

        tenantService.updatePassword(tenant.getId(), new PasswordUpdateRequest(currentPassword, "new_password"));
        verify(tenantRepository, times(1)).findById(tenant.getId());
        verify(tenantRepository, times(1)).save(tenant);
    }

    @Test
    public void updateTenantPassword_but_incorrect_password() {
        Tenant tenant = createTestTenant();
        tenant.setId(UUID.randomUUID().toString());
        when(tenantRepository.findById(tenant.getId())).thenReturn(tenant);

        tenantService.updatePassword(tenant.getId(), new PasswordUpdateRequest("in-correct-password", "new_password"));
        verify(tenantRepository, times(1)).findById(tenant.getId());
    }

    @Test
    public void updateTenant_not_exist_tenant() {
        Tenant tenant = createTestTenant();
        tenant.setId(UUID.randomUUID().toString());
        when(tenantRepository.findById(tenant.getId())).thenReturn(null);

        try {
            tenantService.update(tenant.getId(), tenant);
            fail();
        }catch (RecordNotFoundException e){}
    }

    @Test
    public void activateTenant() {
        Tenant tenant = createTestTenant();
        tenant.setId(UUID.randomUUID().toString());
        when(tenantRepository.findById(tenant.getId())).thenReturn(tenant);
        when(tenantRepository.save(tenant)).thenReturn(tenant);

        tenant = tenantService.activate(tenant.getId());
        verify(tenantRepository, times(1)).findById(tenant.getId());
        verify(tenantRepository, times(1)).save(tenant);
        assertEquals("Activated Tenant", Tenant.Status.ACTIVE, tenant.getStatus());
    }

    @Test
    public void terminateTenant() {
        Tenant tenant = createTestTenant();
        tenant.setId(UUID.randomUUID().toString());
        ApiKeyInformation apiKeyInformation = ApiKeyGenerator.generate(32);
        apiKeyInformation.setId(UUID.randomUUID().toString());
        tenant.setApiKeyInformation(apiKeyInformation);

        when(tenantRepository.findById(tenant.getId())).thenReturn(tenant);
        when(tenantRepository.save(tenant)).thenReturn(tenant);

        tenant = tenantService.terminate(tenant.getId());
        verify(tenantRepository, times(1)).findById(tenant.getId());
        verify(tenantRepository, times(1)).save(tenant);
        assertEquals("Activated Tenant", Tenant.Status.TERMINATED, tenant.getStatus());
        assertTrue("Api Key expired ", tenant.getApiKeyInformation().getExpireDate().compareTo(LocalDateTime.now()) < 1);
    }

    @Test
    public void deleteTenant() {
        Tenant tenant = createTestTenant();
        tenant.setId(UUID.randomUUID().toString());
        ApiKeyInformation testApiKey = ApiKeyGenerator.generate(32);
        testApiKey.setId(UUID.randomUUID().toString());
        tenant.setApiKeyInformation(testApiKey);

        ClientDetails clientDetails = new TestClientDetails();
        when(tenantRepository.findById(tenant.getId())).thenReturn(tenant);
        when(jdbcClientDetailsService.loadClientByClientId(tenant.getLoginCredential().getAccount())).thenReturn(clientDetails);

        tenantService.delete(tenant.getId());
        verify(tenantRepository, times(1)).findById(tenant.getId());
        verify(tenantRepository, times(1)).delete(tenant.getId());
        verify(tenantApiKeyRepository, times(1)).delete(testApiKey.getId());
        verify(jdbcClientDetailsService, times(1)).loadClientByClientId(tenant.getLoginCredential().getAccount());
        verify(jdbcClientDetailsService, times(1)).removeClientDetails(tenant.getLoginCredential().getAccount());
    }

    @Test
    public void deleteTenant_without_api_key() {
        Tenant tenant = createTestTenant();
        tenant.setId(UUID.randomUUID().toString());
        ApiKeyInformation testApiKey = ApiKeyGenerator.generate(32);
        testApiKey.setId(UUID.randomUUID().toString());

        when(tenantRepository.findById(tenant.getId())).thenReturn(tenant);

        tenantService.delete(tenant.getId());
        verify(tenantRepository, times(1)).findById(tenant.getId());
        verify(tenantRepository, times(1)).delete(tenant.getId());
        verify(tenantApiKeyRepository, times(0)).delete(testApiKey.getId());
        verify(jdbcClientDetailsService, times(1)).loadClientByClientId(tenant.getLoginCredential().getAccount());
        verify(jdbcClientDetailsService, times(0)).removeClientDetails(tenant.getLoginCredential().getAccount());
    }

    @Test
    public void addApplication() {
        Tenant tenant = createTestTenant();
        tenant.setId(UUID.randomUUID().toString());
        ApiKeyInformation testApiKey = ApiKeyGenerator.generate(32);
        testApiKey.setId(UUID.randomUUID().toString());

        Application application = EndUserResourceTestUtil.createTestApplication();
        when(tenantRepository.findById(tenant.getId())).thenReturn(tenant);
        when(tenantRepository.save(tenant)).thenReturn(tenant);

        tenantService.addApplication(tenant.getId(), application);

        assertNotNull(tenant.getApplications());
        assertFalse(tenant.getApplications().isEmpty());
        assertTrue(tenant.getApplications().contains(application));
        assertEquals("Verify application owner", application.getOwner(), tenant);
    }

    private Tenant createTestTenant() {
        return TenantTestUtil.createTestTenant();
    }

    private static class TestClientDetails implements ClientDetails {
        @Override
        public String getClientId() {
            return null;
        }

        @Override
        public Set<String> getResourceIds() {
            return null;
        }

        @Override
        public boolean isSecretRequired() {
            return false;
        }

        @Override
        public String getClientSecret() {
            return null;
        }

        @Override
        public boolean isScoped() {
            return false;
        }

        @Override
        public Set<String> getScope() {
            return null;
        }

        @Override
        public Set<String> getAuthorizedGrantTypes() {
            return null;
        }

        @Override
        public Set<String> getRegisteredRedirectUri() {
            return null;
        }

        @Override
        public Collection<GrantedAuthority> getAuthorities() {
            return null;
        }

        @Override
        public Integer getAccessTokenValiditySeconds() {
            return null;
        }

        @Override
        public Integer getRefreshTokenValiditySeconds() {
            return null;
        }

        @Override
        public boolean isAutoApprove(String scope) {
            return false;
        }

        @Override
        public Map<String, Object> getAdditionalInformation() {
            return null;
        }
    }
}
