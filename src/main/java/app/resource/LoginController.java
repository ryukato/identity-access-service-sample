package app.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@RestController
public class LoginController {

    @Autowired
    TokenEndpoint tokenEndpoint;

    @Autowired
    TokenStore tokenStore;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<OAuth2AccessToken> postAccessToken(Principal principal, @RequestParam
            Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
        // TODO: api key check, if there is no, then throws error
        return tokenEndpoint.postAccessToken(principal, parameters);
    }

    @RequestMapping(value = "/oauth/token/revoke",  method = RequestMethod.POST)
    public ResponseEntity<?> revokeToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        Optional.ofNullable(authorization).ifPresent(a -> {
            if (a.contains("Bearer")) {
                String tokenId = authorization.substring("Bearer".length() + 1);
                tokenStore.removeAccessToken(new DefaultOAuth2AccessToken(tokenId));
            }
        });
        return ResponseEntity.ok("{result: 'success'}");
    }

    @RequestMapping(value = "/logout",  method = RequestMethod.POST)
    public ResponseEntity<?> logout(HttpServletRequest request) {
        return revokeToken(request);
    }

}
