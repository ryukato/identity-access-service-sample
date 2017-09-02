package app.util;

import app.domain.Tenant;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component(value = "tenantBaseClientDetailsFactory")
public class TenantBaseClientDetailsFactory implements ClientDetailsFactory<Tenant> {
    private static final List<String> AUTHORIZED_GRANT_TYPES = Arrays.asList(
            "authorization_code",
            "password",
            "client_credentials",
            "implicit",
            "refresh_token");
    private static final List<GrantedAuthority> GRANTED_AUTHORITIES = AuthorityUtils.commaSeparatedStringToAuthorityList("AP_MANAGER,ADMIN");
    private static final List<String> DEFAULT_SCOPES = Arrays.asList("read", "write", "refresh_token");

    @Override
    public ClientDetails createFrom(Tenant tenant) {
        BaseClientDetails details = new BaseClientDetails();

//        details.setClientId(tenant.getId());
        details.setClientId(tenant.getLoginCredential().getAccount());
//        details.setClientSecret(tenant.getApiKeyInformation().getApiKey());
        details.setClientSecret(tenant.getLoginCredential().getPassword());
        details.setAuthorizedGrantTypes(AUTHORIZED_GRANT_TYPES);
        details.setAuthorities(GRANTED_AUTHORITIES);
        details.setScope(DEFAULT_SCOPES);
        details.setRefreshTokenValiditySeconds(3600);
        details.setAccessTokenValiditySeconds(3600);
        details.setRegisteredRedirectUri(Collections.<String>emptySet());
        return details;
    }
}
