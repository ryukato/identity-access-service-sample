package app.domain;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public String getCurrentAuditor() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        return Optional.ofNullable(authentication)
                .map(a -> {
                    if (a.getPrincipal() instanceof String) {
                        return (String) a.getPrincipal();
                    }
                    return ((User) a.getPrincipal()).getUsername();
                })
                .orElse("TEST_ADMIN");
    }
}
