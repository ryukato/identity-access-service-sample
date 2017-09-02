package app.resource;

import app.IdentityAccessServiceApplication;
import app.domain.Application;
import app.domain.Tenant;
import app.repository.ApplicationRepository;
import app.repository.TenantRepository;
import app.util.BeanCopyUtil;
import app.util.ClientDetailsFactory;
import app.util.OAuth2Helper;
import app.util.TenantTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
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

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = IdentityAccessServiceApplication.class)
@WebAppConfiguration
@Transactional
public class ApplicationResourceTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    OAuth2Helper oAuth2Helper;

    @Autowired
    private ClientDetailsFactory<Application> clientDetailsFactory;

    @Autowired
    private JdbcClientDetailsService jdbcClientDetailsService;


    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    private Application createdApp;
    private Application createdApp2;
    private Tenant tenant;
    private RequestPostProcessor bearerToken;
    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().orElse(null);

        assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
    }

    private void prepareTestApplications() {
        this.createdApp = EndUserResourceTestUtil.prepareTestApplications(applicationRepository);
        tenant.getApplications().add(this.createdApp);
        this.createdApp.setOwner(tenant);


        this.createdApp2 = EndUserResourceTestUtil.createTestApplication();
        this.createdApp2.setName("test_application2");
        this.createdApp2 = applicationRepository.save(this.createdApp2);
    }


    @Before
    public void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        Tenant testTenant = TenantTestUtil.createTestTenant();
        testTenant.getLoginCredential().setAccount("test_manager");
        this.tenant = tenantRepository.save(testTenant);
        prepareTestApplications();
        bearerToken = oAuth2Helper.bearerToken("service-portal", "test_manager");
    }

    @Test
    public void findAll() throws Exception {
//        String managerId = tenant.getId(); // TODO mangerId -> tenantId
        String managerId = "test_manager";
        mockMvc.perform(get("/api/applications").param("managerId", managerId).with(bearerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andDo(print())
                .andReturn();
    }

    @Test
    public void findAllOfOwn() throws Exception {
        mockMvc.perform(get("/api/applications/own")
                .with(bearerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andExpect(jsonPath("$.content", hasSize(1))) // verify this tenant has only one application
                .andDo(print())
                .andReturn();
    }

    @Test // DEPPJT-292
    public void findAllOfOwn_with_paging_and_sort() throws Exception {
        mockMvc.perform(get("/api/applications/own")
                .param("page", "0")
                .param("size", "10")
                .param("sort","lastModifiedAt,asc")
                .with(bearerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andExpect(jsonPath("$.content", hasSize(1))) // verify this tenant has only one application
                .andDo(print())
                .andReturn();
    }

    @Test
    public void findById() throws Exception {
        String managerId = "test_manager";
        mockMvc.perform(get("/api/applications/" + createdApp.getId()).param("managerId", managerId).with(bearerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andDo(print())
                .andReturn();
    }

    @Test
    public void findByIdAndOwnerId() throws Exception {
        mockMvc.perform(get("/api/applications/own/" + createdApp.getId()).with(bearerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.id").value(createdApp.getId()))
                .andDo(print())
                .andReturn();
    }

    @Test
    public void findByIdAndOwnerId_wrong_owner() throws Exception {
        mockMvc.perform(get("/api/applications/own/" + createdApp2.getId()).with(bearerToken))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();
    }

    @Test
    public void findById_not_exists() throws Exception {
        String managerId = "test_manager";
        mockMvc.perform(get("/api/applications/" + "not_exists").param("managerId", managerId).with(bearerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createApplication() throws Exception {
        String managerId = "test_manager";
        Application application = createTestApplication();
        application.setName("new application "+ LocalDateTime.now().toString());
        mockMvc.perform(post("/api/applications")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("managerId", managerId).with(bearerToken)
                .content(json(application)))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void createOwnApplication() throws Exception {
        Application application = createTestApplication();
        application.setName("new application "+ LocalDateTime.now().toString());
        mockMvc.perform(post("/api/applications/own")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .with(bearerToken)
                .content(json(application)))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void createOwnApplication_missing_name_property() throws Exception {
        Application application = createTestApplication();
        application.setName(null);
        mockMvc.perform(post("/api/applications/own")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .with(bearerToken)
                .content(json(application)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void createOwnApplication_empty_name_property() throws Exception {
        Application application = createTestApplication();
        application.setName(null);
        mockMvc.perform(post("/api/applications/own")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .with(bearerToken)
                .content(json(application)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test // #DEPPJT-286
    public void createOwnApplication_already_same_app_name_exist() throws Exception {
        Application application = createTestApplication();
        application.setName(createdApp.getName());
        mockMvc.perform(post("/api/applications/own")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .with(bearerToken)
                .content(json(application)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void updateApplication() throws Exception {
        String managerId = "test_manager";
        Application application = this.createdApp;
        application.setName("updated_application");
        mockMvc.perform(put("/api/applications/"+ createdApp.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("managerId", managerId)
                .with(bearerToken)
                .content(json(application)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updated_application"))
                .andDo(print());
    }

    @Test
    public void updateOwnApplication() throws Exception {
        Application application = this.createdApp;
        application.setOwner(tenant);
        application.setName("updated_application");
        mockMvc.perform(put("/api/applications/own/"+ createdApp.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .with(bearerToken)
                .content(json(application)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updated_application"))
                .andExpect(jsonPath("$.owner").value(tenant.getId())) //Fix for #DEPPJT-280
                .andDo(print());
    }

    @Test //Fix for #DEPPJT-289
    public void updateOwnApplication_with_null_values_for_existing() throws Exception {
        Application application = this.createdApp;
        Application updateApplication = new Application();
        BeanCopyUtil.copyNonNullProperties(application, updateApplication);
        updateApplication.setStatus(null);
        updateApplication.setOwner(null);
        updateApplication.setName(null);
        mockMvc.perform(put("/api/applications/own/"+ createdApp.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .with(bearerToken)
                .content(json(updateApplication)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner").value(tenant.getId()))
                .andExpect(jsonPath("$.name").value(this.createdApp.getName()))
                .andExpect(jsonPath("$.status").value(this.createdApp.getStatus().name()));
    }

    @Test
    public void deleteOwnApplication() throws Exception {
        Application application = this.createdApp;
        application.setName("updated_application");
        mockMvc.perform(delete("/api/applications/own/"+ createdApp.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .with(bearerToken))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    // Fix for #DEPPJT-284
    @Test
    public void deleteOwnApplication_not_exist() throws Exception {
        Application application = this.createdApp;
        application.setName("updated_application");
        mockMvc.perform(delete("/api/applications/own/"+ "not_exists_application_id")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .with(bearerToken))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void deleteApplication() throws Exception {
        String managerId = "test_manager";
        Application application = this.createdApp;
        application.setName("updated_application");
        mockMvc.perform(delete("/api/applications/"+ createdApp.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("managerId", managerId)
                .with(bearerToken))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    public void search() throws Exception {
        mockMvc.perform(get("/api/applications/search").with(bearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$",  hasSize(3)))
                .andReturn();
    }

    @Test
    public void search_by_name_like() throws Exception {
        mockMvc.perform(get("/api/applications/search/by-name-like").param("name", createdApp.getName().substring(0, 1)).with(bearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andReturn();
    }

    @Test
    public void search_by_status() throws Exception {
        mockMvc.perform(get("/api/applications/search/by-status").param("status", Application.ApplicationStatus.ACTIVE.name()).with(bearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andReturn();
    }

    @Test
    public void search_by_is_disabled_new_user() throws Exception {
        mockMvc.perform(get("/api/applications/search/by-disabled-new-user").param("disabledNewUser", "false").with(bearerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andReturn();
    }
    private Application createTestApplication() {
        return EndUserResourceTestUtil.createTestApplication();
    }

    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

}
