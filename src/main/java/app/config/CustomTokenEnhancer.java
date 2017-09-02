package app.config;

import app.domain.EndUser;
import app.domain.Tenant;
import app.repository.EndUserRepository;
import app.repository.TenantRepository;
import app.util.EmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.Map;

public class CustomTokenEnhancer implements TokenEnhancer {
    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private EndUserRepository endUserRepository;

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        final Map<String, Object> additionalInfo = new HashMap<>();
        if (authentication.getPrincipal() != null && authentication.getPrincipal().getClass() == String.class) {
            String tenantAccount = (String) authentication.getPrincipal();
            Tenant tenant = tenantRepository.findByLoginCredentialAccount(tenantAccount);
            if (tenant == null) {
                return accessToken;
            }
            additionalInfo.put("id", tenant.getId());
        } else {
            Authentication usernamePasswordAuthToken = SecurityContextHolder.getContext().getAuthentication();
            if (usernamePasswordAuthToken == null) {
                return accessToken;
            }
            String applicationId = ((User) usernamePasswordAuthToken.getPrincipal()).getUsername();
            User user = (User) authentication.getPrincipal();

            EndUser userFromRepository = null;
            if (isEmailLogin(user.getUsername())) {
                userFromRepository = endUserRepository.findByApplicationAndEmail(applicationId, user.getUsername());
            } else {
                userFromRepository = endUserRepository.findByApplicationAndCredentialAccount(applicationId, user.getUsername());
            }

            additionalInfo.put("id", userFromRepository.getId());
        }
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);

        return accessToken;
    }

    private boolean isEmailLogin(String login) {
        return EmailUtil.isEmailLogin(login);
    }
}
