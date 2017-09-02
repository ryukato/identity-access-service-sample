package app.resource;

import app.IdentityAccessServiceApplication;
import app.domain.Application;
import app.repository.ApplicationRepository;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = IdentityAccessServiceApplication.class)
@WebAppConfiguration
@Transactional
public class EndUserResourceGetAccessTokenTest {

    public static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json;charset=UTF-8";
    public static final String TEST_CLIENT_ID_FOR_AP_MANAGER = "test_client_id";
    public static final String TEST_CLIENT_SECRET_FOR_AP_MANAGER = "test_client_secret";

    public static final String TEST_CLIENT_ID_FOR_END_USER  = "test_client_id_for_end_user";
    public static final String TEST_CLIENT_SECRET_FOR_END_USER = "test_client_secret_for_end_user";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private JdbcClientDetailsService jdbcClientDetailsService;

    @Autowired
    ApplicationRepository applicationRepository;

    @Before
    public void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain).build();
    }

    private BaseClientDetails getClientDetailsForApManager() {
        BaseClientDetails testClientDetails = getBaseClientDetails();
        testClientDetails.setClientId(TEST_CLIENT_ID_FOR_AP_MANAGER);
        testClientDetails.setClientSecret(TEST_CLIENT_SECRET_FOR_AP_MANAGER);
        testClientDetails.setScope(Arrays.asList("READ", "WRITE"));
        testClientDetails.setAuthorizedGrantTypes(Arrays.asList("client_credentials"));
        testClientDetails.setAuthorities(Arrays.asList(new SimpleGrantedAuthority("AP_MANAGER")));
        testClientDetails.setRegisteredRedirectUri(null);
        testClientDetails.setAccessTokenValiditySeconds(10);
        testClientDetails.setRefreshTokenValiditySeconds(10);
        return testClientDetails;
    }

    private ClientDetails getClientDetailsForEndUser() {
        BaseClientDetails testClientDetails = getBaseClientDetails();
        testClientDetails.setClientId(TEST_CLIENT_ID_FOR_END_USER);
        testClientDetails.setClientSecret(TEST_CLIENT_SECRET_FOR_END_USER);
        testClientDetails.setAuthorizedGrantTypes(Arrays.asList("password"));
        testClientDetails.setScope(Arrays.asList("READ", "WRITE"));
        testClientDetails.setAuthorities(Arrays.asList(new SimpleGrantedAuthority("USER")));
        testClientDetails.setRegisteredRedirectUri(null);
        testClientDetails.setAccessTokenValiditySeconds(10);
        testClientDetails.setRefreshTokenValiditySeconds(10);
        return testClientDetails;
    }

    private BaseClientDetails getBaseClientDetails() {
        return new BaseClientDetails();
    }

    @Test
    public void getAccessTokenOfApManager() throws Exception {
        ClientDetails testClientDetailsForApManager = getClientDetailsForApManager();
        jdbcClientDetailsService.addClientDetails(testClientDetailsForApManager);
        ResultActions result = mockMvc.perform(
                (post("/oauth/token"))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .params(buildLoginParams())
                        .with(httpBasic(TEST_CLIENT_ID_FOR_AP_MANAGER, TEST_CLIENT_SECRET_FOR_AP_MANAGER))
                        .accept(APPLICATION_JSON_CHARSET_UTF_8)

                ).andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_CHARSET_UTF_8));

        String resultString = result.andReturn().getResponse().getContentAsString();

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        assertThat("Received Access Token", jsonParser.parseMap(resultString).containsKey("access_token"), Is.is(true));

    }

    @Test
    public void getAccessTokenOfEndUser() throws Exception {
        ClientDetails testClientDetailsForEndUser = getClientDetailsForEndUser();
        jdbcClientDetailsService.addClientDetails(testClientDetailsForEndUser);
        Page<Application> applications =  applicationRepository.findAll(new PageRequest(0, 1));
        String applicationId = applications.hasContent() ? applications.getContent().get(0).getId() : TEST_CLIENT_ID_FOR_AP_MANAGER;
        String secret = applications.hasContent() ? applications.getContent().get(0).getApiKey() : TEST_CLIENT_SECRET_FOR_END_USER;
        MultiValueMap<String, String> loginParams = buildLoginParamsForEndUser();
        loginParams.add("applicationId", applicationId);
        ResultActions result = mockMvc.perform(
                (post("/oauth/token"))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .params(loginParams)
                        .with(httpBasic(applicationId, secret))
                        .accept(APPLICATION_JSON_CHARSET_UTF_8)

        ).andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_CHARSET_UTF_8));

        String resultString = result.andReturn().getResponse().getContentAsString();

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        assertThat("Received Access Token", jsonParser.parseMap(resultString).containsKey("access_token"), Is.is(true));

    }

    private MultiValueMap<String, String> buildLoginParamsForEndUser() {
        MultiValueMap<String, String> loginParams = buildLoginParams();
        loginParams.remove("grant_type");
        loginParams.add("grant_type", "password");
        loginParams.add("username", "test_user");
        loginParams.add("password", "test");
        return loginParams;
    }

    private MultiValueMap<String, String>  buildLoginParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
//        params.add("username", "test_user");
//        params.add("password", "test");
        return params;
    }
}
