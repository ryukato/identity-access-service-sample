package app.resource;

import app.IdentityAccessServiceApplication;
import app.domain.Application;
import app.util.ClientDetailsFactory;
import app.util.OAuth2Helper;
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
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = IdentityAccessServiceApplication.class)
@WebAppConfiguration
@Transactional
public class CodeEnumResourceTest {
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
    private ClientDetailsFactory<Application> clientDetailsFactory;

    @Autowired
    private JdbcClientDetailsService jdbcClientDetailsService;


    private HttpMessageConverter mappingJackson2HttpMessageConverter;

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
    }


    @Test
    public void allGenders() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken("service-portal", "test_manager");
        mockMvc.perform(get("/api/codes/gender-types").with(bearerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.[0]").isNotEmpty())
                .andDo(print())
                .andReturn();
    }

    @Test
    public void allEndUserStatus() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken("service-portal", "test_manager");
        mockMvc.perform(get("/api/codes/end-user-status-types").with(bearerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.[0]").isNotEmpty())
                .andDo(print())
                .andReturn();
    }

    @Test
    public void allTenantStatus() throws Exception {
        RequestPostProcessor bearerToken = oAuth2Helper.bearerToken("service-portal", "test_manager");
        mockMvc.perform(get("/api/codes/tenant-status-types").with(bearerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.[0]").isNotEmpty())
                .andDo(print())
                .andReturn();
    }

}
