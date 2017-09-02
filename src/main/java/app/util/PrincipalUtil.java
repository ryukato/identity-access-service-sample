package app.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.security.Principal;
import java.util.Collection;

public final class PrincipalUtil {
    private PrincipalUtil() {}

    public static String getUserNameFrom(Principal principal) {
        Object principal1 = ((Authentication) principal).getPrincipal();
        if (principal1 instanceof String) {
            return (String) principal1;
        } else {
            return ((User) principal1).getUsername();
        }
    }

    public static Collection<? extends GrantedAuthority> getUserAuthorities(Principal principal) {
        Object principal1 = ((Authentication) principal).getPrincipal();
        if (principal1 instanceof String) {
            return ((Authentication) principal).getAuthorities();
        } else {
            return ((User) principal1).getAuthorities();
        }
    }
}
