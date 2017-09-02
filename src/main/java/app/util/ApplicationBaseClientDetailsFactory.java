package app.util;

import app.domain.Application;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component(value = "applicationBaseClientDetailsFactory")
public class ApplicationBaseClientDetailsFactory implements ClientDetailsFactory<Application> {

    private static final List<String> AUTHORIZED_GRANT_TYPES = Arrays.asList(
            "authorization_code",
            "password",
            "client_credentials",
            "implicit",
            "refresh_token");
    private static final List<GrantedAuthority> GRANTED_AUTHORITIES = AuthorityUtils.commaSeparatedStringToAuthorityList("USER");
    private static final List<String> DEFAULT_SCOPES = Arrays.asList("write", "read", "refresh_token");

    @Override
    public ClientDetails createFrom(Application application) {
        BaseClientDetails details = new BaseClientDetails();

        details.setClientId(application.getId());
        details.setClientSecret(application.getApiKey());
        details.setAuthorizedGrantTypes(application.getAuthorizedGrantTypes() == null || application.getAuthorizedGrantTypes().isEmpty() ? AUTHORIZED_GRANT_TYPES : application.getAuthorizedGrantTypes());
        details.setAuthorities(application.getAuthorities() == null || application.getAuthorities().isEmpty() ? GRANTED_AUTHORITIES : application.getAuthorities());
        details.setScope(application.getScopes() == null || application.getScopes().isEmpty() ? DEFAULT_SCOPES : application.getScopes());
        details.setRegisteredRedirectUri(Optional.ofNullable(application.getRegisteredRedirectUris()).orElse(Collections.<String>emptySet()));
        // TODO: RefreshTokenValiditySeconds, AccessTokenValiditySeconds from application
        details.setRefreshTokenValiditySeconds(3600);
        details.setAccessTokenValiditySeconds(3600);
        return details;
    }

}
