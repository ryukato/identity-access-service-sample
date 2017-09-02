package app.resource;

import app.IdentityAccessServiceApplication;
import app.domain.Application;
import app.domain.EndUser;
import app.domain.PasswordUpdateRequest;
import app.repository.ApplicationRepository;
import app.repository.EndUserRepository;
import app.util.ClientDetailsFactory;
import app.util.OAuth2Helper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.IsNot;
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
import org.springframework.security.core.authority.AuthorityUtils;
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
import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = IdentityAccessServiceApplication.class)
@WebAppConfiguration
@Transactional
public class EndUserResourceTestWithSecurityTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EndUserRepository endUserRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ClientDetailsFactory<Application> clientDetailsFactory;

    @Autowired
    private JdbcClientDetailsService jdbcClientDetailsService;

    @Autowired
    OAuth2Helper oAuth2Helper;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    private Application createdApp;
    private EndUser endUser;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {
        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters)
                .stream()
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);
        assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
    }

    @Before
    public void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        prepareTestApplications();
        ClientDetails clientDetails = clientDetailsFactory.createFrom(this.createdApp);
        jdbcClientDetailsService.addClientDetails(clientDetails);
        prepareTestUsers();
        prepareApplicationEndUsersForDev();
    }

    private void prepareTestApplications() {
        this.createdApp = EndUserResourceTestUtil.prepareTestApplications(applicationRepository, AuthorityUtils.commaSeparatedStringToAuthorityList("USER"));
    }

    private void prepareTestUsers() {
        this.endUser = EndUserResourceTestUtil.prepareTestUsers(passwordEncoder, endUserRepository);
        this.endUser.getCredential().setAccount("test_updated_end_user");
        endUserRepository.save(this.endUser);
    }

    private void prepareApplicationEndUsersForDev() {
        EndUserResourceTestUtil.prepareApplicatonEndUsersForDev(this.createdApp, this.endUser, applicationRepository);
    }

    @Test
    public void updateEndUserPassword() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken(this.createdApp.getId(), endUser.getCredential().getAccount());
        String applicationId = createdApp.getId();
        EndUser newEndUser = endUser;
        newEndUser.setEmail("test_updated_end_user@test.com");

        String prePassword = endUser.getCredential().getPassword();
        mockMvc.perform(put("/api/end-users/" + endUser.getId() + "/password")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId)
                .content(json(new PasswordUpdateRequest("test", "new_password")))
                .with(bearerToken)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credential.password").isNotEmpty())
                .andExpect(jsonPath("$.credential.password").value(IsNot.not(prePassword)));
    }

    @Test
    public void updateEndUserPassword_with_different_user() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken(this.createdApp.getId(), "different_end_user");
        String applicationId = createdApp.getId();
        EndUser newEndUser = endUser;
        newEndUser.setEmail("test_updated_end_user@test.com");

        String prePassword = endUser.getCredential().getPassword();
        mockMvc.perform(put("/api/end-users/" + endUser.getId() + "/password")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId)
                .content(json(new PasswordUpdateRequest("test", "new_password")))
                .with(bearerToken)
        )
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    public void updateEndUserProfile() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken(this.createdApp.getId(), endUser.getCredential().getAccount());
        String applicationId = createdApp.getId();
        EndUser newEndUser = endUser;
        newEndUser.setEmail("test_updated_end_user@test.com");

        String prePassword = endUser.getCredential().getPassword();
        mockMvc.perform(put("/api/end-users/" + endUser.getId() + "/profile")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId)
                .content(json(endUser))
                .with(bearerToken)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").isNotEmpty())
                .andExpect(jsonPath("$.email").value(newEndUser.getEmail()));
    }

    @Test
    public void updateEndUserProfile_with_different_user() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken(this.createdApp.getId(), "different_end_user");
        String applicationId = createdApp.getId();
        EndUser newEndUser = endUser;
        newEndUser.setEmail("test_updated_end_user@test.com");

        String prePassword = endUser.getCredential().getPassword();
        mockMvc.perform(put("/api/end-users/" + endUser.getId() + "/profile")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId)
                .content(json(endUser))
                .with(bearerToken)
        )
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    public void updateEndUserProfile_with_admin_account() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken("service-portal", "test_manager");
        String applicationId = createdApp.getId();
        EndUser newEndUser = endUser;
        newEndUser.setEmail("test_updated_end_user@test.com");

        String prePassword = endUser.getCredential().getPassword();
        mockMvc.perform(put("/api/end-users/" + endUser.getId() + "/profile")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId)
                .content(json(endUser))
                .with(bearerToken)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").isNotEmpty())
                .andExpect(jsonPath("$.email").value(newEndUser.getEmail()));
    }

    @Test
    public void updateEndUser_but_user_not_exist() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken("service-portal", "test_manager");
        String applicationId = createdApp.getId();
        EndUser newEndUser = endUser;
        newEndUser.setEmail("test_updated_end_user@test.com");
        mockMvc.perform(put("/api/end-users/not_exist_end_user_id")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId)
                .content(json(newEndUser))
                .with(bearerToken)
        )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateEndUser() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken("service-portal", "test_manager");
        String applicationId = createdApp.getId();
        EndUser newEndUser = endUser;
        newEndUser.setEmail("test_updated_end_user@test.com");
        mockMvc.perform(put("/api/end-users/" + endUser.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("applicationId", applicationId)
                .content(json(newEndUser))
                .with(bearerToken)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").isNotEmpty())
                .andExpect(jsonPath("$.email").value(endUser.getEmail()))
        ;
    }



    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
