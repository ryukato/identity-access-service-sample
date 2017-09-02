package app.resource;

import app.IdentityAccessServiceApplication;
import app.domain.ApiKeyInformation;
import app.domain.LoginCredential;
import app.domain.PasswordUpdateRequest;
import app.domain.Tenant;
import app.repository.TenantApiKeyRepository;
import app.repository.TenantRepository;
import app.service.TenantService;
import app.util.ClientDetailsFactory;
import app.util.OAuth2Helper;
import app.util.TenantTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = IdentityAccessServiceApplication.class)
@WebAppConfiguration
@Transactional
public class TenantResourceTest {
    private final LocalDateTime testExpireDate = LocalDateTime.of(2017,8,30,1,1);

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    OAuth2Helper oAuth2Helper;

    @Autowired
    @Qualifier("tenantBaseClientDetailsFactory")
    private ClientDetailsFactory<Tenant> clientDetailsFactory;

    @Autowired
    private JdbcClientDetailsService jdbcClientDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;


    private Tenant tenant;
    private ApiKeyInformation apiKeyInformation;

    @Autowired
    TenantRepository tenantRepository;

    @Autowired
    TenantService tenantService;

    @Autowired
    TenantApiKeyRepository tenantApiKeyRepository;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().orElse(null);

        assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
    }

    @Before
    public void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext).apply(springSecurity())
                .build();

        prepareTestTenants();
        ClientDetails clientDetails = clientDetailsFactory.createFrom(this.tenant);
        jdbcClientDetailsService.addClientDetails(clientDetails);
    }

    private void prepareTestTenants() {
        tenant = TenantTestUtil.prepareTestTenants("test_tenant_1", tenantRepository, tenantApiKeyRepository, testExpireDate);
    }

    @Test
    public void findAllTenants() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken("sysadmin", "sysadmin");
        mockMvc.perform(get("/api/tenants").with(bearerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andDo(print());
    }

    @Test // DEPPJT-292
    public void findAllTenants_with_page_and_sort() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken("sysadmin", "sysadmin");
        mockMvc.perform(get("/api/tenants")
                .param("page", "0")
                .param("size", "10")
                .param("sort","lastModifiedAt,asc")
                .with(bearerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andDo(print());
    }

    @Test
    public void findATenant() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken(tenant.getLoginCredential().getAccount(), tenant.getLoginCredential().getAccount());
        mockMvc.perform(get("/api/tenants/" + tenant.getId()).with(bearerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andDo(print());
    }

    @Test
    public void findAllApplications() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken(tenant.getLoginCredential().getAccount(), tenant.getLoginCredential().getAccount());
        mockMvc.perform(get("/api/tenants/" + tenant.getId() + "/applications").with(bearerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print());
    }

    @Test // DEPPJT-292
    public void findAllApplications_with_page_and_sort() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken(tenant.getLoginCredential().getAccount(), tenant.getLoginCredential().getAccount());
        mockMvc.perform(get("/api/tenants/" + tenant.getId() + "/applications")
                .param("page", "0")
                .param("size", "10")
                .param("sort","lastModifiedAt,asc")
                .with(bearerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print());
    }

    @Test
    public void create_Tenant() throws Exception {
        LoginCredential loginCredential = new LoginCredential("test_new_tenant", "password");
        Tenant newTenant = TenantTestUtil.createTestTenant(
                "test_new_tenant@test.com",
                "test_company_name"
                , loginCredential
                );
        mockMvc.perform(post("/api/tenants/register").contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(json(newTenant)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void create_Tenant_without_login_credential() throws Exception {
        LoginCredential loginCredential = null;
        Tenant newTenant = TenantTestUtil.createTestTenant(
                "test_new_tenant@test.com",
                "test_company_name"
                , loginCredential
        );
        mockMvc.perform(post("/api/tenants/register").contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(json(newTenant)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void create_Tenant_without_login_credential_account() throws Exception {
        String nullAccount = null;
        LoginCredential loginCredential = new LoginCredential(nullAccount, "password");
        Tenant newTenant = TenantTestUtil.createTestTenant(
                "test_new_tenant@test.com",
                "test_company_name"
                , loginCredential
        );
        mockMvc.perform(post("/api/tenants/register").contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(json(newTenant)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void create_Tenant_without_login_credential_password() throws Exception {
        String nullPassword = null;
        LoginCredential loginCredential = new LoginCredential("test_new_tenant", nullPassword);
        Tenant newTenant = TenantTestUtil.createTestTenant(
                "test_new_tenant@test.com",
                "test_company_name"
                , loginCredential
        );
        mockMvc.perform(post("/api/tenants/register").contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(json(newTenant)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    @Test
    public void create_Tenant_with_duplicated_account() throws Exception {
        LoginCredential loginCredential = new LoginCredential(this.tenant.getLoginCredential().getAccount(), "password");
        Tenant newTenant = TenantTestUtil.createTestTenant(
                "test_new_tenant@test.com",
                "test_company_name"
                , loginCredential
        );
        mockMvc.perform(post("/api/tenants/register").contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(json(newTenant)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void create_Tenant_with_duplicated_email() throws Exception {
        LoginCredential loginCredential = new LoginCredential("test_new_tenant", "password");
        Tenant newTenant = TenantTestUtil.createTestTenant(
                this.tenant.getEmail(),
                "test_company_name"
                , loginCredential
        );
        mockMvc.perform(post("/api/tenants/register").contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(json(newTenant)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

    @Test
    public void updateTenant() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken(tenant.getLoginCredential().getAccount(), tenant.getLoginCredential().getAccount());
        Tenant updateTenant = tenantRepository.findByLoginCredentialAccount(tenant.getLoginCredential().getAccount());
        String updatedEmail = "updated_email@test.com";
        updateTenant.setEmail(updatedEmail);
        mockMvc.perform(
                put("/api/tenants/" + updateTenant.getId())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json(updateTenant))
                        .with(bearerToken)
        )
                .andExpect(status().isOk())
                .andDo(print());

        updateTenant = tenantRepository.findByLoginCredentialAccount(tenant.getLoginCredential().getAccount());
        assertEquals("Updated Email", updatedEmail, updateTenant.getEmail());
    }

    @Test
    public void updateTenantPassword() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken(tenant.getLoginCredential().getAccount(), tenant.getLoginCredential().getAccount());
        Tenant updateTenant = tenantRepository.findByLoginCredentialAccount(tenant.getLoginCredential().getAccount());
        String currentPassword = updateTenant.getLoginCredential().getPassword();
        updateTenant.getLoginCredential().setPassword(passwordEncoder.encode(currentPassword));
        tenantRepository.save(updateTenant);

        assertTrue(passwordEncoder.matches(currentPassword, updateTenant.getLoginCredential().getPassword()));
        mockMvc.perform(
                put("/api/tenants/" + updateTenant.getId() + "/password")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json(new PasswordUpdateRequest(currentPassword, "new_password")))
                        .with(bearerToken)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginCredential.password").isNotEmpty())
                .andDo(print());

        updateTenant = tenantRepository.findByLoginCredentialAccount(tenant.getLoginCredential().getAccount());
        assertFalse(passwordEncoder.matches(currentPassword, updateTenant.getLoginCredential().getPassword()));
    }

    @Test
    public void activate() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken(tenant.getLoginCredential().getAccount(), tenant.getLoginCredential().getAccount());
        Tenant updateTenant = tenantRepository.findByLoginCredentialAccount(tenant.getLoginCredential().getAccount());

        mockMvc.perform(
                put("/api/tenants/" + updateTenant.getId() + "/activation")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json(updateTenant))
                        .with(bearerToken)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").isNotEmpty())
                .andExpect(jsonPath("$.status").value(Is.is("ACTIVE")))
                .andDo(print());
    }

    @Test
    public void inactivate() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken(tenant.getLoginCredential().getAccount(), tenant.getLoginCredential().getAccount());
        Tenant updateTenant = tenantRepository.findByLoginCredentialAccount(tenant.getLoginCredential().getAccount());

        mockMvc.perform(
                put("/api/tenants/" + updateTenant.getId() + "/inactivation")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json(updateTenant))
                        .with(bearerToken)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").isNotEmpty())
                .andExpect(jsonPath("$.status").value(Is.is("INACTIVE")))
                .andDo(print());
    }

    @Test
    public void lock() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken(tenant.getLoginCredential().getAccount(), tenant.getLoginCredential().getAccount());
        Tenant updateTenant = tenantRepository.findByLoginCredentialAccount(tenant.getLoginCredential().getAccount());

        mockMvc.perform(
                put("/api/tenants/" + updateTenant.getId() + "/lock")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json(updateTenant))
                        .with(bearerToken)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").isNotEmpty())
                .andExpect(jsonPath("$.status").value(Is.is("LOCKED")))
                .andDo(print());
    }

    @Test
    public void termination() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken(tenant.getLoginCredential().getAccount(), tenant.getLoginCredential().getAccount());
        Tenant updateTenant = tenantRepository.findByLoginCredentialAccount(tenant.getLoginCredential().getAccount());

        mockMvc.perform(
                put("/api/tenants/" + updateTenant.getId() + "/termination")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json(updateTenant))
                        .with(bearerToken)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").isNotEmpty())
                .andExpect(jsonPath("$.status").value(Is.is("TERMINATED")))
                .andDo(print());
    }
    @Test
    public void terminate() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken(tenant.getLoginCredential().getAccount(), tenant.getLoginCredential().getAccount());
        Tenant updateTenant = tenantRepository.findByLoginCredentialAccount(tenant.getLoginCredential().getAccount());

        mockMvc.perform(
                put("/api/tenants/" + updateTenant.getId() + "/termination")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json(updateTenant))
                        .with(bearerToken)
        )
                .andExpect(status().isOk())
                .andDo(print());

        ApiKeyInformation apiKeyInformation = tenantApiKeyRepository.findOne(tenant.getApiKeyInformation().getId());
        assertTrue(apiKeyInformation.getExpireDate().compareTo(LocalDateTime.now()) < 1);
        updateTenant = tenantRepository.findByLoginCredentialAccount(tenant.getLoginCredential().getAccount());
        assertEquals("Tenant status is Terminated", Tenant.Status.TERMINATED, updateTenant.getStatus());
    }

    @Test
    public void deleteTenant() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken("sysadmin", "sysadmin");
        String deleteTenantId = tenant.getId();
        String clientAccount = tenant.getLoginCredential().getAccount();
        String apiKeyInformationId = tenant.getApiKeyInformation().getId();
        mockMvc.perform(
                delete("/api/tenants/" + deleteTenantId)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(bearerToken)
        )
                .andExpect(status().isNoContent())
                .andDo(print());

        ApiKeyInformation apiKeyInformation = tenantApiKeyRepository.findOne(apiKeyInformationId);
        assertNull("Removed apiKeyInformation", apiKeyInformation);
        Tenant deletedTenant = tenantRepository.findById(deleteTenantId);
        assertNull("Removed Tenant", deletedTenant);

        try{
            jdbcClientDetailsService.loadClientByClientId(clientAccount);
            fail();
        }catch (NoSuchClientException t) {}
    }


}
