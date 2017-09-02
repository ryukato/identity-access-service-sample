package app.resource;

import app.IdentityAccessServiceApplication;
import app.domain.*;
import app.repository.ApplicationRepository;
import app.repository.EndUserRepository;
import app.repository.TenantApiKeyRepository;
import app.repository.TenantRepository;
import app.util.ClientDetailsFactory;
import app.util.OAuth2Helper;
import app.util.TenantTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class EndUserResourceTest {
    private final LocalDateTime testExpireDate = LocalDateTime.of(2017,8,30,1,1);
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EndUserRepository endUserRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    TenantApiKeyRepository tenantApiKeyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    OAuth2Helper oAuth2Helper;

    @Autowired
    @Qualifier("applicationBaseClientDetailsFactory")
    private ClientDetailsFactory<Application> applicationClientDetailsFactory;

    @Autowired
    @Qualifier("tenantBaseClientDetailsFactory")
    private ClientDetailsFactory<Tenant> tenantClientDetailsFactory;

    @Autowired
    private JdbcClientDetailsService jdbcClientDetailsService;


    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    private Application createdApp;
    private EndUser endUser;
    private Tenant tenant;

    private RequestPostProcessor tenantBearerToken;
    private ClientDetails tenantClientDetails;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().orElse(null);

        assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
    }



    @Before
    public void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext).apply(springSecurity())
                .build();
        tenant = TenantTestUtil.prepareTestTenants("test_tenant_1", tenantRepository, tenantApiKeyRepository, testExpireDate);
        prepareTestApplications();
        ClientDetails appClientDetails = applicationClientDetailsFactory.createFrom(this.createdApp);
        jdbcClientDetailsService.addClientDetails(appClientDetails);

        tenantClientDetails = tenantClientDetailsFactory.createFrom(this.tenant);
        jdbcClientDetailsService.addClientDetails(tenantClientDetails);

        prepareTestUsers();
        prepareApplicatonEndUsersForDev();

        tenantBearerToken = oAuth2Helper.bearerToken(
                tenantClientDetails.getClientId(),
                tenant.getLoginCredential().getAccount());

    }

    private void prepareTestApplications() {
        this.createdApp = EndUserResourceTestUtil.prepareTestApplications(applicationRepository);
        tenant.getApplications().add(this.createdApp);
        this.createdApp.setOwner(tenant);
    }

    private void prepareTestUsers() {
        this.endUser = EndUserResourceTestUtil.prepareTestUsers(passwordEncoder, endUserRepository);
    }

    private void prepareApplicatonEndUsersForDev() {
        EndUserResourceTestUtil.prepareApplicatonEndUsersForDev(this.createdApp, this.endUser, applicationRepository);
    }

    private EndUser createTestEndUser(String userName) {
        return EndUserResourceTestUtil.createTestEndUser(userName);
    }

    private EndUser createTestEndUser(String userName, String email) {
        return EndUserResourceTestUtil.createTestEndUser(userName, email);
    }

    private EndUser createTestEndUser(String userName, String email, String mobileNo) {
        return EndUserResourceTestUtil.createTestEndUser(userName, email, mobileNo);
    }

    @Test
    public void findAllEndUsers() throws Exception {
        String applicationId = createdApp.getId();
        mockMvc.perform(get("/api/end-users")
                .param("applicationId", applicationId).with(tenantBearerToken)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andDo(print())
                .andReturn();
    }

    @Test // DEPPJT-292
    public void findAllEndUsers_with_page_and_sort() throws Exception {
        String applicationId = createdApp.getId();
        mockMvc.perform(get("/api/end-users")
                .param("applicationId", applicationId)
                .param("page", "0")
                .param("size", "10")
                .param("sort","lastModifiedAt,asc")
                .with(tenantBearerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andDo(print())
                .andReturn();
    }

    @Test
    public void findAnEndUser() throws Exception {
        String applicationId = createdApp.getId();
        String existingEndUserId = endUser.getId();

        mockMvc.perform(get("/api/end-users/" + existingEndUserId)
                .param("applicationId", applicationId).with(tenantBearerToken)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.id").value(endUser.getId()))
                .andDo(print());
    }

    @Test
    public void createEndUser() throws Exception {
        String applicationId = createdApp.getId();
        EndUser newEndUser = createTestEndUser("test_new_end_user", "test_new_end_user@test.com", "010-111-9999");
        mockMvc.perform(post("/api/end-users")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId).with(tenantBearerToken)
                .content(json(newEndUser)))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void createEndUser_without_login_credential() throws Exception {
        String applicationId = createdApp.getId();
        EndUser newEndUser = createTestEndUser("test_new_end_user", "test_new_end_user@test.com", "010-111-9999");
        newEndUser.setCredential(null);
        mockMvc.perform(post("/api/end-users")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId).with(tenantBearerToken)
                .content(json(newEndUser)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void createEndUser_without_login_credential_account() throws Exception {
        String applicationId = createdApp.getId();
        EndUser newEndUser = createTestEndUser("test_new_end_user", "test_new_end_user@test.com", "010-111-9999");
        newEndUser.getCredential().setAccount(null);
        mockMvc.perform(post("/api/end-users")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId).with(tenantBearerToken)
                .content(json(newEndUser)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void createEndUser_without_login_credential_password() throws Exception {
        String applicationId = createdApp.getId();
        EndUser newEndUser = createTestEndUser("test_new_end_user", "test_new_end_user@test.com", "010-111-9999");
        newEndUser.getCredential().setPassword(null);
        mockMvc.perform(post("/api/end-users")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId).with(tenantBearerToken)
                .content(json(newEndUser)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void createEndUser_with_duplicated_email() throws Exception {
        String applicationId = createdApp.getId();
        EndUser newEndUser = createTestEndUser("test_new_end_user", endUser.getEmail());
        mockMvc.perform(post("/api/end-users")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId).with(tenantBearerToken)
                .content(json(newEndUser)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void createEndUser_with_duplicated_username() throws Exception {
        String applicationId = createdApp.getId();
        EndUser newEndUser = createTestEndUser(endUser.getCredential().getAccount());
        mockMvc.perform(post("/api/end-users")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId).with(tenantBearerToken)
                .content(json(newEndUser)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void createEndUser_with_duplicated_mobileNo() throws Exception {
        String applicationId = createdApp.getId();
        EndUser newEndUser = createTestEndUser("test_new_end_user", "test_new_end_user@test.com", endUser.getProfile().getMobilePhoneNo());
        mockMvc.perform(post("/api/end-users")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId).with(tenantBearerToken)
                .content(json(newEndUser)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void registerEndUser() throws Exception {
        String applicationId = createdApp.getId();
        EndUser newEndUser = createTestEndUser("test_new_end_user", "test_new_end_user@test.com", "010-111-9999");
        mockMvc.perform(post("/api/end-users/register")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId).with(tenantBearerToken)
                .content(json(newEndUser)))
                .andExpect(status().isCreated())
                .andDo(print());

        newEndUser = endUserRepository.findByApplicationAndEmail(createdApp.getId(), newEndUser.getEmail());
        Application testApp = applicationRepository.findOne(createdApp.getId());
        assertFalse(testApp.getApplicationEndUsers().isEmpty());
        ApplicationEndUserId applicationEndUserId = new ApplicationEndUserId();
        applicationEndUserId.setApplication(testApp);
        applicationEndUserId.setEndUser(newEndUser);
        ApplicationEndUser applicationEndUser = new ApplicationEndUser();
        applicationEndUser.setPk(applicationEndUserId);
        assertTrue(testApp.getApplicationEndUsers().contains(applicationEndUser));
        assertEquals(newEndUser.getTenantId(), createdApp.getOwner().getId());
    }

    @Test
    public void unRegisterEndUser() throws Exception {
        String endUserId = endUser.getId();
        String applicationId = createdApp.getId();
        mockMvc.perform(put("/api/end-users/"+endUserId+"/un-registration")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId).with(tenantBearerToken))
                .andExpect(status().isOk())
                .andDo(print());

    }

    @Test
    public void findByFirstNameLike() throws Exception {
        String searchFirstName = endUser.getProfile().getFirstName();
        String applicationId = createdApp.getId();
        mockMvc.perform(get("/api/end-users/search/by-firstName-like")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId)
                .param("firstName", searchFirstName.substring(0, 2))
                .with(tenantBearerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andDo(print());
    }

    @Test
    public void findByFirstName() throws Exception {
        String searchFirstName = endUser.getProfile().getFirstName();
        String applicationId = createdApp.getId();
        mockMvc.perform(get("/api/end-users/search/by-firstName")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId)
                .param("firstName", searchFirstName)
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty());
    }

    @Test
    public void findByFirstNameLike_without_applicationId() throws Exception {
        String searchFirstName = endUser.getProfile().getFirstName();
        mockMvc.perform(get("/api/end-users/search/by-firstName-like")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("firstName", searchFirstName.substring(0, 2))
                .with(tenantBearerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andDo(print());
    }

    @Test
    public void findByFirstName_without_applicationId() throws Exception {
        String searchFirstName = endUser.getProfile().getFirstName();
        mockMvc.perform(get("/api/end-users/search/by-firstName")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("firstName", searchFirstName)
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty());
    }

    @Test
    public void findByLastNameLike() throws Exception {
        String lastName = endUser.getProfile().getLastName();
        String applicationId = createdApp.getId();
        mockMvc.perform(get("/api/end-users/search/by-lastName-like")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId)
                .param("lastName", lastName.substring(0, 2))
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty());
    }

    @Test
    public void findByLastName() throws Exception {
        String lastName = endUser.getProfile().getLastName();
        String applicationId = createdApp.getId();
        mockMvc.perform(get("/api/end-users/search/by-lastName")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId)
                .param("lastName", lastName)
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty());
    }

    @Test
    public void findByMiddleNameLike() throws Exception {
        String middleName = endUser.getProfile().getMiddleName();
        String applicationId = createdApp.getId();
        mockMvc.perform(get("/api/end-users/search/by-middleName-like")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId)
                .param("middleName", middleName.substring(0, 2))
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty());
    }

    @Test
    public void findByLastName_without_applicationId() throws Exception {
        String lastName = endUser.getProfile().getLastName();
        mockMvc.perform(get("/api/end-users/search/by-lastName")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("lastName", lastName)
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty());
    }

    @Test
    public void findByMiddleNameLike_without_applicationId() throws Exception {
        String middleName = endUser.getProfile().getMiddleName();
        mockMvc.perform(get("/api/end-users/search/by-middleName-like")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("middleName", middleName.substring(0, 2))
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty());
    }

    @Test
    public void findByMiddleName() throws Exception {
        String middleName = endUser.getProfile().getMiddleName();
        String applicationId = createdApp.getId();
        mockMvc.perform(get("/api/end-users/search/by-middleName")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId)
                .param("middleName", middleName)
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty());
    }



    @Test
    public void findByEmailLike() throws Exception {
        String email = endUser.getEmail();
        String applicationId = createdApp.getId();
        mockMvc.perform(get("/api/end-users/search/by-email-like")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId)
                .param("email", email.substring(0, 2))
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty());
    }

    @Test
    public void findByMiddleName_without_applicationId() throws Exception {
        String middleName = endUser.getProfile().getMiddleName();
        mockMvc.perform(get("/api/end-users/search/by-middleName")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("middleName", middleName)
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty());
    }



    @Test
    public void findByEmailLike_without_applicationId() throws Exception {
        String email = endUser.getEmail();
        mockMvc.perform(get("/api/end-users/search/by-email-like")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("email", email.substring(0, 2))
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty());
    }

    @Test
    public void findByEmail() throws Exception {
        String email = endUser.getEmail();
        String applicationId = createdApp.getId();
        mockMvc.perform(get("/api/end-users/search/by-email")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId)
                .param("email", email)
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    public void findByEmail_without_applicationId() throws Exception {
        String email = endUser.getEmail();
        String applicationId = createdApp.getId();
        mockMvc.perform(get("/api/end-users/search/by-email")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("email", email)
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    public void findByMobileNo() throws Exception {
        String mobilePhoneNo = endUser.getProfile().getMobilePhoneNo();
        String applicationId = createdApp.getId();
        mockMvc.perform(get("/api/end-users/search/by-mobileNo")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId)
                .param("mobileNo", mobilePhoneNo)
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    public void findByGender() throws Exception {
        String gender = endUser.getProfile().getGender().name();
        String applicationId = createdApp.getId();
        mockMvc.perform(get("/api/end-users/search/by-gender")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId)
                .param("gender", gender)
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty());
    }

    @Test
    public void findByMobileNo_without_applicationId() throws Exception {
        String mobilePhoneNo = endUser.getProfile().getMobilePhoneNo();
        mockMvc.perform(get("/api/end-users/search/by-mobileNo")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("mobileNo", mobilePhoneNo)
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    public void findByGender_without_applicationId() throws Exception {
        String gender = endUser.getProfile().getGender().name();
        mockMvc.perform(get("/api/end-users/search/by-gender")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("gender", gender)
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty());
    }

    @Test
    public void findByNickNameLike() throws Exception {
        String nickName = endUser.getProfile().getNickName();
        String applicationId = createdApp.getId();
        mockMvc.perform(get("/api/end-users/search/by-nickname-like")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId)
                .param("nickname", nickName.substring(0, 2))
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty());
    }

    @Test
    public void findByNickName() throws Exception {
        String nickName = endUser.getProfile().getNickName();
        String applicationId = createdApp.getId();
        mockMvc.perform(get("/api/end-users/search/by-nickname")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId)
                .param("nickname", nickName)
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty());
    }@Test
    public void findByNickNameLike_witout_applicationId() throws Exception {
        String nickName = endUser.getProfile().getNickName();
        mockMvc.perform(get("/api/end-users/search/by-nickname-like")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("nickname", nickName.substring(0, 2))
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty());
    }

    @Test
    public void findByNickName_witout_applicationId() throws Exception {
        String nickName = endUser.getProfile().getNickName();
        mockMvc.perform(get("/api/end-users/search/by-nickname")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("nickname", nickName)
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty());
    }

    @Test
    public void searchUrls() throws Exception {
        mockMvc.perform(get("/api/end-users/search")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.[0].method").isNotEmpty());
    }

    @Test
    public void deleteEndUser() throws Exception {
        String applicationId = createdApp.getId();
        mockMvc.perform(delete("/api/end-users/" + this.endUser.getId())
                .param("applicationId", applicationId)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    public void findByLastName_with_invalid_tenant() throws Exception {
        tenantBearerToken = oAuth2Helper.bearerToken(
                tenantClientDetails.getClientId(),
                "invalid_tenant_account");
        String lastName = endUser.getProfile().getLastName();
        String applicationId = createdApp.getId();
        mockMvc.perform(get("/api/end-users/search/by-lastName")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId)
                .param("lastName", lastName)
                .with(tenantBearerToken))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
