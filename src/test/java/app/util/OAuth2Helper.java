package app.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Collections;

@Component
public class OAuth2Helper {
    @Autowired
    ClientDetailsService clientDetailsService;

    @Autowired
    AuthorizationServerTokenServices  tokenService;

    // For use with MockMvc
    public RequestPostProcessor bearerToken(final String clientId, String userName) {
        return mockRequest -> {
            OAuth2AccessToken token = createAccessToken(clientId, userName);
            mockRequest.addHeader("Authorization", "Bearer " + token.getValue());
            return mockRequest;
        };
    }

    private OAuth2AccessToken createAccessToken(final String clientId, String userName) {
        // Look up authorities, resourceIds and scopes based on clientId
        ClientDetails client = clientDetailsService.loadClientByClientId(clientId);

        // Create request
        OAuth2Request oAuth2Request = new OAuth2Request(
                Collections.emptyMap(),
                clientId,
                client.getAuthorities(),
                true,
                client.getScope(),
                client.getResourceIds(),
                null,
                Collections.emptySet(),
                Collections.emptyMap());

        // Create OAuth2AccessToken
        User userPrincipal = new User(userName, "", true, true, true, true, client.getAuthorities());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userPrincipal, null, client.getAuthorities());
        OAuth2Authentication auth = new OAuth2Authentication(oAuth2Request, authenticationToken);
        return tokenService.createAccessToken(auth);
    }

}
