package app.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class CustomLogoutSuccessHandler extends AbstractAuthenticationTargetUrlRequestHandler implements LogoutSuccessHandler {
    private static final String HEADER_AUTHORIZATION = "authorization";
    private static final String BEARER_AUTHENTICATION = "Bearer ";

    @Autowired
    private TokenStore tokenStore;


    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        final String token = request.getHeader(HEADER_AUTHORIZATION);
        Optional.ofNullable(token).ifPresent(t -> {
            if (t.startsWith(BEARER_AUTHENTICATION)) {
                OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(t.split(" ")[0]);
                Optional.ofNullable(oAuth2AccessToken).ifPresent(oauthToken -> {
                    tokenStore.removeAccessToken(oauthToken);
                });
            }
        });

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
