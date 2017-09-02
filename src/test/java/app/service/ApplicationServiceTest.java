package app.service;

import app.domain.Application;
import app.domain.Tenant;
import app.error.SameApplicationNameExistException;
import app.repository.ApplicationRepository;
import app.repository.EndUserRepository;
import app.util.ClientDetailsFactory;
import app.util.TenantTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ApplicationServiceTest {
    @Mock
    private EndUserRepository endUserRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JdbcClientDetailsService jdbcClientDetailsService;

    @Mock
    private ClientDetailsFactory clientDetailsFactory;

    @Mock
    private TenantService tenantService;

    private ApplicationService applicationService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private EndUserService endUserService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Before
    public void setUp() {
        this.applicationService = new ApplicationService(
                applicationRepository,
                jdbcClientDetailsService,
                clientDetailsFactory,
                tenantService);
        this.endUserService = new EndUserService(endUserRepository, applicationService, passwordEncoder);
    }

    @After
    public void tearDown() {}

    @Test
    public void createApplication() {
        Tenant tenant = TenantTestUtil.createTestTenant();
        tenant.setId("test_tenant_id");
        String testManagerId = "test_manager";
        Application application = createTestApplication(testManagerId);
        ClientDetails testClientDetails = createTestClientDetails();
        when(applicationRepository.findByNameAndOwnerId(application.getName(), tenant.getId(), new PageRequest(0, 1))).thenReturn(new PageImpl<Application>(Collections.emptyList()));
        when(applicationRepository.save(application)).thenReturn(application);
        when(tenantService.getTenantFromAccount(testManagerId)).thenReturn(tenant);
        when(tenantService.addApplication(testManagerId, application)).thenReturn(true);
        when(clientDetailsFactory.createFrom(application)).thenReturn(testClientDetails);
        applicationService.createApplication(testManagerId, application);

        assertEquals(application.getStatus(), Application.ApplicationStatus.CREATED);
        verify(applicationRepository,times(1)).save(application);
        verify(jdbcClientDetailsService,times(1)).addClientDetails(testClientDetails);
    }

    @Test // #DEPPJT-286
    public void createApplication_already_same_app_name_exist() {
        Tenant tenant = TenantTestUtil.createTestTenant();
        tenant.setId("test_tenant_id");
        String testManagerId = "test_manager";
        Application application = createTestApplication(testManagerId);
        Application application2 = createTestApplication(testManagerId);
        ClientDetails testClientDetails = createTestClientDetails();
        when(applicationRepository.findByNameAndOwnerId(application.getName(), tenant.getId(), new PageRequest(0, 1)))
                .thenReturn(new PageImpl<Application>(Collections.singletonList(application2)));
        when(applicationRepository.save(application)).thenReturn(application);
        when(tenantService.getTenantFromAccount(testManagerId)).thenReturn(tenant);
        when(tenantService.addApplication(testManagerId, application)).thenReturn(true);
        when(clientDetailsFactory.createFrom(application)).thenReturn(testClientDetails);

        try {
            applicationService.createApplication(testManagerId, application);
            fail();
        }catch (SameApplicationNameExistException e){}
    }

    @Deprecated
    @Test
    public void updateApplication() {
        String testManagerId = "test_manager";
        Application application = createTestApplication(testManagerId);
        String testApplicationId = "test_application";
        application.setId(testApplicationId);
        when(applicationRepository.findByIdAndManagerId(testManagerId, testApplicationId)).thenReturn(application);
        when(applicationRepository.save(application)).thenReturn(application);

        applicationService.updateApplication(testApplicationId, testManagerId, application);
        verify(applicationRepository,times(1)).save(application);

    }

    @Test
    public void updateApplicationOf() {
        Tenant testTenant = TenantTestUtil.createTestTenant();
        Application application = createTestApplication(testTenant.getLoginCredential().getAccount());
        String testApplicationId = "test_application";
        application.setId(testApplicationId);
        when(tenantService.getTenantFromAccount(testTenant.getLoginCredential().getAccount())).thenReturn(testTenant);
        when(applicationRepository.findOne(testApplicationId)).thenReturn(application);
        when(applicationRepository.save(application)).thenReturn(application);

        testTenant.getApplications().add(application);

        applicationService.updateApplicationOf(testTenant.getLoginCredential().getAccount(), testApplicationId, application);
        verify(applicationRepository,times(1)).save(application);

        // Fix for #DEPPJT-280
        assertEquals("Verify owner account", testTenant.getLoginCredential().getAccount(), application.getOwner().getLoginCredential().getAccount());
    }

    @Test
    public void deleteApplication() {
        String testApplicationId = "test_application";

        boolean result = applicationService.deleteApplication(testApplicationId);
        assertTrue(result);
        verify(applicationRepository,times(1)).delete(testApplicationId);
    }

    private Application createTestApplication(String managerId) {
        Application application = new Application();
        application.setName("test_application");
        application.setManagerId(managerId);
        application.setDisabledNewUser(false);

        return application;
    }


    private ClientDetails createTestClientDetails() {
        BaseClientDetails details = new BaseClientDetails();

        details.setClientId("test_client");
        details.setClientSecret("test_client_secret");
        details.setAuthorizedGrantTypes(Arrays.asList("authorization_code",
                "password", "client_credentials", "implicit", "refresh_token"));
        details.setAuthorities(
                AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
        details.setRegisteredRedirectUri(Collections.<String>emptySet());
        return details;
    }
}
