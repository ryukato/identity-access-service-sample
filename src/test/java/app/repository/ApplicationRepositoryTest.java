package app.repository;

import app.IdentityAccessServiceApplication;
import app.domain.Application;
import app.domain.Tenant;
import app.util.TenantTestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = IdentityAccessServiceApplication.class)
@Transactional
public class ApplicationRepositoryTest {
    private static final PageRequest PAGE_REQUEST = new PageRequest(0, 10);

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant tenant;
    private Application application;
    @Before
    public void setUp() {
        tenant = TenantTestUtil.createTestTenant();
        tenantRepository.save(tenant);

        application = new Application();
        application.setManagerId("test_manager");
        application.setName("test_application");
        application.setStatus(Application.ApplicationStatus.ACTIVE);
        application.setDisabledNewUser(false);
        application.setApiKey("test_api_key");
        applicationRepository.save(application);

        application.setOwner(tenant);
        tenant.getApplications().add(application);
    }

    @Test
    public void findByManagerId(){
        Page<Application> applications = applicationRepository.findByManagerId("test_manager", PAGE_REQUEST);
        assertTrue(applications.hasContent());
    }

    @Test
    public void findByOwnerId() {
        Page<Application> applications = applicationRepository.findByOwnerId(tenant.getId(), PAGE_REQUEST);
        assertTrue(applications.hasContent());
    }

    @Test
    public void findByIdAndManagerId() {
        Application searchedApp = applicationRepository.findByIdAndManagerId(application.getId(), "test_manager");
        assertNotNull(searchedApp);
    }

    @Test
    public void findByNameAndManagerId() {
        Page<Application> searchedApps = applicationRepository.findByNameAndManagerId("test_application", "test_manager", PAGE_REQUEST);
        assertNotNull(searchedApps);
    }

    @Test // #DEPPJT-286
    public void findByNameAndOwnerId() {
        Page<Application> searchedApps = applicationRepository.findByNameAndOwnerId("test_application", tenant.getId(), PAGE_REQUEST);
        assertNotNull(searchedApps);
        assertTrue(searchedApps.hasContent());
    }

    @Test
    public void findByNameLikeAndOwnerAccount() {
        String appName = application.getName();
        Page<Application> searchedApps = applicationRepository.findByNameLikeAndOwnerAccount(
                appName,
                tenant.getLoginCredential().getAccount(),
                PAGE_REQUEST);
        assertNotNull(searchedApps);
        assertTrue(searchedApps.hasContent());

        String appLikeName = appName.substring(0, 1);
        searchedApps = applicationRepository.findByNameLikeAndOwnerAccount(
                appLikeName,
                tenant.getLoginCredential().getAccount(),
                PAGE_REQUEST);
        assertNotNull(searchedApps);
        assertTrue(searchedApps.hasContent());

        searchedApps = applicationRepository.findByNameLikeAndOwnerAccount(
                "no_result_application_name",
                tenant.getLoginCredential().getAccount(),
                PAGE_REQUEST);
        assertNotNull(searchedApps);
        assertFalse(searchedApps.hasContent());
    }

    @Test
    public void findByStatusAndOwnerAccount() {
        Page<Application> searchedApps = applicationRepository.findByStatusAndOwnerAccount(
                Application.ApplicationStatus.ACTIVE,
                tenant.getLoginCredential().getAccount(),
                PAGE_REQUEST);
        assertNotNull(searchedApps);
        assertTrue(searchedApps.hasContent());

        searchedApps = applicationRepository.findByStatusAndOwnerAccount(
                Application.ApplicationStatus.TERMINATED,
                tenant.getLoginCredential().getAccount(),
                PAGE_REQUEST);
        assertNotNull(searchedApps);
        assertFalse(searchedApps.hasContent());
    }

    @Test
    public void findByIsDisabledNewUserAndOwnerAccount() {
        Page<Application> searchedApps = applicationRepository.findByIsDisabledNewUserAndOwnerAccount(
                false,
                tenant.getLoginCredential().getAccount(),
                PAGE_REQUEST);
        assertNotNull(searchedApps);
        assertTrue(searchedApps.hasContent());

        searchedApps = applicationRepository.findByIsDisabledNewUserAndOwnerAccount(
                true,
                tenant.getLoginCredential().getAccount(),
                PAGE_REQUEST);
        assertNotNull(searchedApps);
        assertFalse(searchedApps.hasContent());
    }


    @Test
    public void findByNameLike() {
        String appName = application.getName();
        Page<Application> searchedApps = applicationRepository.findByNameLike(appName, PAGE_REQUEST);
        assertNotNull(searchedApps);
        assertTrue(searchedApps.hasContent());

        String appLikeName = appName.substring(0, 1);
        searchedApps = applicationRepository.findByNameLike(appLikeName, PAGE_REQUEST);
        assertNotNull(searchedApps);
        assertTrue(searchedApps.hasContent());

        searchedApps = applicationRepository.findByNameLike("no_result_application_name", PAGE_REQUEST);
        assertNotNull(searchedApps);
        assertFalse(searchedApps.hasContent());
    }

    @Test
    public void findByStatus() {
        Page<Application> searchedApps = applicationRepository.findByStatus(
                Application.ApplicationStatus.ACTIVE,
                PAGE_REQUEST);
        assertNotNull(searchedApps);
        assertTrue(searchedApps.hasContent());

        searchedApps = applicationRepository.findByStatus(
                Application.ApplicationStatus.TERMINATED,
                PAGE_REQUEST);
        assertNotNull(searchedApps);
        assertFalse(searchedApps.hasContent());
    }

    @Test
    public void findByIsDisabledNewUser() {
        Page<Application> searchedApps = applicationRepository.findByIsDisabledNewUser(false, PAGE_REQUEST);
        assertNotNull(searchedApps);
        assertTrue(searchedApps.hasContent());

        searchedApps = applicationRepository.findByIsDisabledNewUser(true, PAGE_REQUEST);
        assertNotNull(searchedApps);
        assertFalse(searchedApps.hasContent());
    }
}
