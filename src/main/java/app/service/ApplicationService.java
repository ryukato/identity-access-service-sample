package app.service;

import app.domain.Application;
import app.domain.ApplicationEndUser;
import app.domain.EndUser;
import app.domain.Tenant;
import app.error.InvalidApplicationOwnerException;
import app.error.MissingRequiredPropertyException;
import app.error.RecordNotFoundException;
import app.error.SameApplicationNameExistException;
import app.repository.ApplicationRepository;
import app.util.ApiKeyGenerator;
import app.util.BeanCopyUtil;
import app.util.ClientDetailsFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ApplicationService {
    private static final int DEFAULT_API_KEY_LENGTH = 32;
    private final ApplicationRepository applicationRepository;

    private final JdbcClientDetailsService jdbcClientDetailsService;
    private final ClientDetailsFactory<Application> clientDetailsFactory;
    private final TenantService tenantService;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            JdbcClientDetailsService jdbcClientDetailsService,
            @Qualifier("applicationBaseClientDetailsFactory") ClientDetailsFactory<Application> clientDetailsFactory,
            TenantService tenantService) {
        this.applicationRepository = applicationRepository;
        this.jdbcClientDetailsService = jdbcClientDetailsService;
        this.clientDetailsFactory = clientDetailsFactory;
        this.tenantService = tenantService;
    }

    public boolean addApplicationUser(String applicationId, EndUser endUser) {
        try {
            Application application = applicationRepository.findOne(applicationId);
            Optional.ofNullable(application).ifPresent(application1 -> {
                ApplicationEndUser applicationEndUser = new ApplicationEndUser();
                applicationEndUser.setApplication(application1);
                applicationEndUser.setEndUser(endUser);

                application1.getApplicationEndUsers().add(applicationEndUser);
                endUser.setTenantId(application.getOwner().getId());
                endUser.getApplicationEndUsers().add(applicationEndUser);
//                applicationRepository.save(application1);
            });
            return true;
        }catch (Throwable t) {
            return false;
        }
    }

    public Application createApplication(String managerId, String tenantAccount, Application newApplication) {
        Optional.ofNullable(managerId).ifPresent(mId -> {newApplication.setManagerId(managerId);});
        Optional.ofNullable(newApplication.getName()).orElseThrow(() -> new MissingRequiredPropertyException("Application name is required"));
        verifyAppNameIsNotEmpty(newApplication);
        checkSameApplicationNameExist(tenantAccount, newApplication.getName());
        return createApplication(tenantAccount, newApplication);
    }

    public Application createApplication(String tenantAccount, Application newApplication) {
        Optional.ofNullable(newApplication.getName()).orElseThrow(() -> new MissingRequiredPropertyException("Application name is required"));
        verifyAppNameIsNotEmpty(newApplication);
        checkSameApplicationNameExist(tenantAccount, newApplication.getName());
        newApplication.setStatus(Application.ApplicationStatus.CREATED);
        newApplication.setApiKey(ApiKeyGenerator.generate(DEFAULT_API_KEY_LENGTH).getApiKey());
        Application createdApplication = applicationRepository.save(newApplication);
        tenantService.addApplicationToTenant(tenantAccount, createdApplication);
        ClientDetails clientDetails = buildClientDetails(createdApplication);
        jdbcClientDetailsService.addClientDetails(clientDetails);
        return createdApplication;
    }

    private void verifyAppNameIsNotEmpty(Application newApplication) {
        if (newApplication.getName().isEmpty()) {
            throw new MissingRequiredPropertyException("Application name is required");
        }
    }

    private ClientDetails buildClientDetails(Application application) {
        return clientDetailsFactory.createFrom(application);
    }

    private void checkSameApplicationNameExist(String tenantAccount, String applicationName) {
        Tenant tenant = tenantService.getTenantFromAccount(tenantAccount);
        Page<Application> applications = applicationRepository.findByNameAndOwnerId(applicationName, tenant.getId(), new PageRequest(0, 1));
        if (applications.hasContent() && !applications.getContent().isEmpty()) {
            throw new SameApplicationNameExistException(String.format("Same application name(%s) is being used.", applicationName));
        }
    }

    @Deprecated
    public Application updateApplication(String managerId, String id, Application application) {
        Application existingApplication = getExistingApplication(managerId, id);
        BeanCopyUtil.copyNonNullProperties(application, existingApplication);
        return applicationRepository.save(existingApplication);
    }

    public Application updateApplicationOf(String tenantAccount, String applicationId, Application application) {
        Application existingApplication = getExistingApplication(applicationId);
        Tenant tenant = tenantService.getTenantFromAccount(tenantAccount);
        if (!tenant.getApplications().contains(existingApplication)) {
            throw new InvalidApplicationOwnerException(String.format("Tenant(%s) does NOT have application: %s", tenantAccount, application.getId()));
        }
        existingApplication.setOwner(tenant);
        BeanCopyUtil.copyNonNullProperties(application, existingApplication);
        return applicationRepository.save(existingApplication);
    }

    private Application getExistingApplication(String managerId, String id) {
        Application existingApplication = applicationRepository.findByIdAndManagerId(id, managerId);
        return Optional.ofNullable(existingApplication).<RecordNotFoundException>orElseThrow(RecordNotFoundException::new);
    }

    private Application getExistingApplication(String applicationId) {
        Application existingApplication = applicationRepository.findOne(applicationId);
        return Optional.ofNullable(existingApplication).<RecordNotFoundException>orElseThrow(RecordNotFoundException::new);
    }

    @Deprecated
    public boolean deleteApplication(String applicationId) {
        try {
            jdbcClientDetailsService.removeClientDetails(applicationId);
            applicationRepository.delete(applicationId);
            return true;
        }catch (Throwable t) {
            return false;
        }
    }

    public boolean deleteApplication(String tenantAccount, String id) {
        try {
            Application application = applicationRepository.findOne(id);
            Tenant tenant = tenantService.getTenantFromAccount(tenantAccount);
            if (!tenant.getApplications().contains(application)) {
                throw new InvalidApplicationOwnerException(String.format("Tenant(%s) does NOT have application: %s", tenantAccount, application.getId()));
            }
            applicationRepository.delete(application.getId());
            return true;
        }catch (Throwable t){
            return false;
        }
    }

    public void checkApplicationExists(String applicationId) {
        Application application = applicationRepository.findOne(applicationId);
        Optional.ofNullable(application).<RecordNotFoundException>orElseThrow(() -> new RecordNotFoundException(String.format("Application: %s does not exist", application)));
    }


    public Page<Application> findAllOf(String tenantAccount, Pageable pageable) {
        Tenant tenant = tenantService.getTenantFromAccount(tenantAccount);
        return applicationRepository.findByOwnerId(tenant.getId(), pageable);
    }

    public Application findOneOf(String tenantAccount, String applicationId) {
        Tenant tenant = tenantService.getTenantFromAccount(tenantAccount);
        return applicationRepository.findByIdAndOwnerId(applicationId, tenant.getId());
    }
}
