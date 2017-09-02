package app.security;

import app.domain.EndUser;
import app.repository.EndUserRepository;
import app.util.EmailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/*
// TODO : throws UserNotActivatedException when user is not active
// TODO: get user authorities from database or other configuration.
 */
@Service("domainUserDetailsService")
public class DomainUserDetailsService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(DomainUserDetailsService.class);


    private final EndUserRepository endUserRepository;

    @Autowired
    public DomainUserDetailsService(EndUserRepository endUserRepository) {
        this.endUserRepository = endUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Authenticating {}", login);
        String lowercaseLogin = login.toLowerCase();
        String applicationId = ((User) authentication.getPrincipal()).getUsername();
        if (StringUtils.isEmpty(applicationId)) {
            throw new InvalidClientException("client_id(application id) is required");
        }
        EndUser userFromRepository = null;
        if (isEmailLogin(login)) {
            userFromRepository = endUserRepository.findByApplicationAndEmail(applicationId, lowercaseLogin);
        } else {
            userFromRepository = endUserRepository.findByApplicationAndCredentialAccount(applicationId, lowercaseLogin);
        }

        return buildLoadedUserDetails(login, userFromRepository);
    }

    private UserDetails buildLoadedUserDetails(String login, EndUser userFromRepository) {
        return Optional.ofNullable(userFromRepository).map(u -> {
            Collection<GrantedAuthority> grantedAuthorities = Arrays.asList(new SimpleGrantedAuthority("USER"));
            return new User(
                    u.getCredential().getAccount(),
                    u.getCredential().getPassword(),
                    grantedAuthorities);
        }).<UsernameNotFoundException>orElseThrow(() -> new UsernameNotFoundException(String.format("User: %s was not found", login)));
    }

    private boolean isEmailLogin(String login) {
        return EmailUtil.isEmailLogin(login);
    }
}
