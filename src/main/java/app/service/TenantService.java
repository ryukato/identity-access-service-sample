package app.service;

import app.domain.*;
import app.error.DuplicatedEmailUserFoundException;
import app.error.NoUserLoginCredentialException;
import app.error.RecordNotFoundException;
import app.error.SameUserNameFoundException;
import app.repository.TenantApiKeyRepository;
import app.repository.TenantRepository;
import app.util.ApiKeyGenerator;
import app.util.ClientDetailsFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Consumer;

@Service
public class TenantService {
    private static final int DEFAULT_API_KEY_LENGTH = 32;
    private final TenantRepository tenantRepository;
    private final TenantApiKeyRepository tenantApiKeyRepository;
    private final ClientDetailsFactory<Tenant> tenantClientDetailsFactory;
    private final JdbcClientDetailsService jdbcClientDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public TenantService(
            TenantRepository tenantRepository,
            TenantApiKeyRepository tenantApiKeyRepository,
            @Qualifier("tenantBaseClientDetailsFactory")  ClientDetailsFactory tenantClientDetailsFactory,
            JdbcClientDetailsService jdbcClientDetailsService,
            PasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.tenantApiKeyRepository = tenantApiKeyRepository;
        this.tenantClientDetailsFactory = tenantClientDetailsFactory;
        this.jdbcClientDetailsService = jdbcClientDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    public Tenant createTenant(Tenant tenant) {
        validateLoginCredentialExists(tenant);
        checkSameTenantAccountExists(tenant);
        checkSameTenantEmailExists(tenant);

        tenant.setStatus(Tenant.Status.CREATED);
        if (tenant.getProfile() == null) {
            tenant.setProfile(new UserProfile());
        }
        Tenant createdTenant = tenantRepository.save(tenant);
        configApiKeyInformationTo(createdTenant);
        createdTenant = tenantRepository.save(createdTenant);
        ClientDetails clientDetails = tenantClientDetailsFactory.createFrom(tenant);
        jdbcClientDetailsService.addClientDetails(clientDetails);
        // TODO : notify Tenant registered Event
        return createdTenant;
    }

    private void validateLoginCredentialExists(Tenant tenant) {
        LoginCredential loginCredential = tenant.getLoginCredential();
        Optional.ofNullable(loginCredential).<NoUserLoginCredentialException>orElseThrow(NoUserLoginCredentialException::new);
        Optional<String> account = Optional.ofNullable(loginCredential.getAccount());
        account.<NoUserLoginCredentialException>orElseThrow(NoUserLoginCredentialException::new);
        Optional<String> password = Optional.ofNullable(loginCredential.getPassword());
        password.<NoUserLoginCredentialException>orElseThrow(NoUserLoginCredentialException::new);

        account.filter(a -> a.isEmpty()).ifPresent(a -> new NoUserLoginCredentialException());
        password.filter(a -> a.isEmpty()).ifPresent(a -> new NoUserLoginCredentialException());
    }

    private void checkSameTenantEmailExists(Tenant tenant) {
        Tenant existingTenant = tenantRepository.findByEmail(tenant.getEmail());
        Optional.ofNullable(existingTenant)
                .ifPresent(t ->
                        {
                            throw new DuplicatedEmailUserFoundException(String.format("%s already existing", tenant.getLoginCredential().getAccount()));
                        }
                );
    }

    private void checkSameTenantAccountExists(Tenant tenant) {
        Tenant existingTenant = tenantRepository.findByLoginCredentialAccount(tenant.getLoginCredential().getAccount());
        Optional.ofNullable(existingTenant)
                .ifPresent(t ->
                        {
                            throw new SameUserNameFoundException(String.format("%s already existing", tenant.getLoginCredential().getAccount()));
                        }
                );
    }

    private void configApiKeyInformationTo(Tenant tenant) {
        ApiKeyInformation apiKeyInformation = ApiKeyGenerator.generate(DEFAULT_API_KEY_LENGTH);
        tenant.setApiKeyInformation(apiKeyInformation);

        apiKeyInformation.setOwner(tenant);
    }

    public Tenant activate(String id) {
        // TODO : notify Tenant activated Event
        return changeTenantStatus(id, Tenant.Status.ACTIVE);
    }

    public Tenant inactivate(String id) {
        // TODO : notify Tenant in-activated Event
        return changeTenantStatus(id, Tenant.Status.INACTIVE);
    }

    public Tenant lock(String id) {
        // TODO : notify Tenant locked Event
        return changeTenantStatus(id, Tenant.Status.LOCKED);
    }

    public Tenant terminate(String id) {
        Tenant tenant = changeTenantStatus(id, Tenant.Status.TERMINATED);
        expireTenantApiKeyInformation(tenant);
        // TODO : notify Tenant terminated Event
        return tenant;
    }

    private void expireTenantApiKeyInformation(Tenant tenant) {
        handleTenantApiKeyInformation(tenant, apiKeyInformation -> apiKeyInformation.setExpireDate(LocalDateTime.now()));
    }

    private void deleteTenantApiKeyInformation(Tenant deletedTenant) {
        handleTenantApiKeyInformation(deletedTenant, apiKeyInformation -> tenantApiKeyRepository.delete(deletedTenant.getApiKeyInformation().getId()));
    }

    private void handleTenantApiKeyInformation(Tenant tenant, Consumer<ApiKeyInformation> consumer) {
        Optional.ofNullable(tenant.getApiKeyInformation()).ifPresent(consumer);
    }

    public boolean delete(String id) {
        try {
            Tenant deletedTenant = tenantRepository.findById(id);
            deleteTenantApiKeyInformation(deletedTenant);

            try {
                ClientDetails clientDetails = jdbcClientDetailsService.loadClientByClientId(deletedTenant.getLoginCredential().getAccount());
                Optional.ofNullable(clientDetails).ifPresent(
                        clientDetails1 -> {
                            jdbcClientDetailsService.removeClientDetails(deletedTenant.getLoginCredential().getAccount());
                        });
            }catch (NoSuchClientException e) {
                //do nothing
            }
            tenantRepository.delete(id);
            return true;
        }catch (Throwable t) {
            return false;
        }
    }

    private Tenant changeTenantStatus(String id, Tenant.Status newStatus) {
        Tenant tenant = tenantRepository.findById(id);
        Optional.ofNullable(tenant)
                .<RecordNotFoundException>orElseThrow(RecordNotFoundException::new);
        tenant.setStatus(newStatus);
        tenantRepository.save(tenant);
        return tenant;
    }

    public Tenant update(String id, Tenant tenantToUpdate) {
        boolean doUpdate = false;
        Tenant existingTenant = findExistingTenant(id);
        if (tenantToUpdate.getEmail() != null &&
                !tenantToUpdate.getEmail().isEmpty() &&
                !tenantToUpdate.getEmail().equals(existingTenant.getEmail())) {
            existingTenant.setEmail(tenantToUpdate.getEmail());
            doUpdate = true;
        }
        if (tenantToUpdate.getProfile() != null && !tenantToUpdate.getProfile().equals(existingTenant.getProfile())) {
            existingTenant.setProfile(tenantToUpdate.getProfile());
            doUpdate = true;
        }
        if (doUpdate) {
            return tenantRepository.save(tenantToUpdate);
        } else {
            return existingTenant;
        }
    }

    private Tenant findExistingTenant(String id) {
        Tenant tenant = tenantRepository.findById(id);
        Optional.ofNullable(tenant)
                .<RecordNotFoundException>orElseThrow(RecordNotFoundException::new);
        return tenant;
    }

    public boolean addApplication(String tenantId, Application application) {
        Tenant tenant = Optional.ofNullable(tenantRepository.findById(tenantId))
                .<RecordNotFoundException>orElseThrow(RecordNotFoundException::new);
        try {
            tenant.getApplications().add(application);
            application.setOwner(tenant);
            tenantRepository.save(tenant);
            return true;
        }catch (Throwable t) {
            return false;
        }
    }

    public boolean addApplicationToTenant(String tenantAccount, Application application) {
        Tenant tenant = Optional.ofNullable(tenantRepository.findByLoginCredentialAccount(tenantAccount))
                .<RecordNotFoundException>orElseThrow(RecordNotFoundException::new);
        return addApplication(tenant.getId(), application);
    }

    public Tenant updatePassword(String id, PasswordUpdateRequest passwordUpdateRequest) {
        Tenant tenant = findExistingTenant(id);
        LoginCredential loginCredential = tenant.getLoginCredential();
        String encodedPassword = loginCredential.getPassword();
        if (passwordEncoder.matches(passwordUpdateRequest.getCurrentPassword(), encodedPassword)) {
            loginCredential.setPassword(passwordEncoder.encode(passwordUpdateRequest.getNewPassword()));
            return tenantRepository.save(tenant);
        }
        return tenant;

    }

    Tenant getTenantFromAccount(String tenantAccount) {
        return Optional.ofNullable(tenantRepository.findByLoginCredentialAccount(tenantAccount))
                .<RecordNotFoundException>orElseThrow(RecordNotFoundException::new);
    }
}
