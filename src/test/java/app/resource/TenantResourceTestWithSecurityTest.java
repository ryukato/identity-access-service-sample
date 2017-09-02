package app.resource;

import app.IdentityAccessServiceApplication;
import app.domain.ApiKeyInformation;
import app.domain.LoginCredential;
import app.domain.Tenant;
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

import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = IdentityAccessServiceApplication.class)
@WebAppConfiguration
@Transactional
public class TenantResourceTestWithSecurityTest {
    private final LocalDateTime testExpireDate = LocalDateTime.of(2017,8,30,1,1);
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;

    @Autowired
    ObjectMapper mapper;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;


    private Tenant tenant;
    private ApiKeyInformation apiKeyInformation;

    @Autowired
    TenantRepository tenantRepository;

    @Autowired
    TenantApiKeyRepository tenantApiKeyRepository;

    @Autowired
    @Qualifier("tenantBaseClientDetailsFactory")
    private ClientDetailsFactory<Tenant> clientDetailsFactory;

    @Autowired
    private JdbcClientDetailsService jdbcClientDetailsService;

    @Autowired
    OAuth2Helper oAuth2Helper;


    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().orElse(null);

        assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
    }

    @Before
    public void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        prepareTestTenants();

        ClientDetails clientDetails = clientDetailsFactory.createFrom(this.tenant);
        jdbcClientDetailsService.addClientDetails(clientDetails);
    }

    @Test
    public void update_Tenant() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken(this.tenant.getLoginCredential().getAccount(), tenant.getLoginCredential().getAccount());
        LoginCredential loginCredential = new LoginCredential("test_new_tenant", "password");
        Tenant newTenant = this.tenant;
        newTenant.setCompanyName("updated_company_name");
        mockMvc.perform(put("/api/tenants/" + tenant.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(json(newTenant))
                .with(bearerToken)
        )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void update_Tenant_with_not_allowec_tenant() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken(this.tenant.getLoginCredential().getAccount(), "not_allowed_tenant");
        LoginCredential loginCredential = new LoginCredential("not_allowed_tenant", "password");
        Tenant newTenant = this.tenant;
        newTenant.setCompanyName("updated_company_name");
        mockMvc.perform(put("/api/tenants/" + tenant.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(json(newTenant))
                .with(bearerToken)
        )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }


    private void prepareTestTenants() {
        tenant = TenantTestUtil.prepareTestTenants("test_tenant_1", tenantRepository, tenantApiKeyRepository, testExpireDate);
    }

    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
