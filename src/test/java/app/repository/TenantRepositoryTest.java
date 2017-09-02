package app.repository;

import app.IdentityAccessServiceApplication;
import app.domain.ApiKeyInformation;
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

import java.time.LocalDateTime;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = IdentityAccessServiceApplication.class)
@Transactional
public class TenantRepositoryTest {
    private static final PageRequest PAGE_REQUEST = new PageRequest(0, 10);
    private final LocalDateTime testExpireDate = LocalDateTime.of(2017,8,30,1,1);

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private TenantApiKeyRepository tenantApiKeyRepository;

    private Tenant tenant;
    private ApiKeyInformation apiKeyInformation;
    @Before
    public void setUp() {
        tenant = TenantTestUtil.createTestTenant();
        apiKeyInformation = new ApiKeyInformation(tenant.getId(), testExpireDate);
        tenant.setApiKeyInformation(apiKeyInformation);
        apiKeyInformation.setOwner(tenant);
        tenantRepository.save(tenant);
        tenantApiKeyRepository.save(apiKeyInformation);
    }


    @Test
    public void findAll() {
        Page<Tenant> tenants = tenantRepository.findAll(PAGE_REQUEST);
        assertTrue("findAll there is at least one tenant", tenants.hasContent());
    }

    @Test
    public void findById() {
        Tenant tenant = tenantRepository.findById(this.tenant.getId());
        assertNotNull(tenant);
    }

    @Test
    public void findByEmail() {
        Tenant tenant = tenantRepository.findByEmail(this.tenant.getEmail());
        assertNotNull(tenant);
    }

    @Test
    public void findByEmailLike() {
        Page<Tenant> tenants = tenantRepository.findByEmailLike("t", PAGE_REQUEST);
        assertTrue("findAll there is at least one tenant", tenants.hasContent());
    }

    @Test
    public void findByLoginCredentialAccount() {
        Tenant tenant = tenantRepository.findByLoginCredentialAccount(this.tenant.getLoginCredential().getAccount());
        assertNotNull(tenant);
    }
    @Test
    public void findByLoginCredentialAccountLike() {
        Page<Tenant> tenants = tenantRepository.findByLoginCredentialAccountLike("t" ,PAGE_REQUEST);
        assertTrue("findAll there is at least one tenant", tenants.hasContent());
    }

    @Test
    public void findByApiKeyInformationApiKey() {
        Tenant tenant = tenantRepository.findByApiKeyInformationApiKey(this.tenant.getApiKeyInformation().getApiKey());
        assertNotNull(tenant);
    }





}
