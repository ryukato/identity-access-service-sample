package app.resource;

import app.domain.Application;
import app.error.RecordNotFoundException;
import app.repository.ApplicationRepository;
import app.service.ApplicationService;
import app.util.PrincipalUtil;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/applications")
@Api(value = "identity-access-service", description = "Operations to manage applications belongs to a ap manager")
public class ApplicationResource {

    private final ApplicationRepository applicationRepository;
    private final ApplicationService applicationService;

    @Autowired
    public ApplicationResource(ApplicationRepository applicationRepository, ApplicationService applicationService) {
        this.applicationRepository = applicationRepository;
        this.applicationService = applicationService;
    }

    //  TODO: managerId -> tenantId
    @Deprecated
    @RequestMapping(method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('ADMIN','AP_MANAGER')")
    public ResponseEntity<Page<Application>> findAll(
            @RequestParam("managerId") String managerId,
            Pageable pageable) {
        Page<Application> applications = applicationRepository.findByManagerId(managerId, pageable);
        return ResponseEntity.ok(applications);
    }

    @RequestMapping(value = "/own", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('ADMIN','AP_MANAGER')")
    public ResponseEntity<Page<Application>> findAllOfTenant(
            Principal principal,
            Pageable pageable) {
        String tenantAccount = PrincipalUtil.getUserNameFrom(principal);
        Page<Application> applications = applicationService.findAllOf(tenantAccount, pageable);
        return ResponseEntity.ok(applications);
    }

    @Deprecated
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('ADMIN', 'AP_MANAGER')")
    public ResponseEntity<Application> findById(
            @PathVariable("id") String id,
            @RequestParam("managerId") String managerId
    ) {
        Application application = applicationRepository.findByIdAndManagerId(id, managerId);
        Optional.ofNullable(application).<RecordNotFoundException>orElseThrow(() -> new RecordNotFoundException());
        return ResponseEntity.ok(application);
    }

    @RequestMapping(value = "/own/{id}", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('ADMIN', 'AP_MANAGER')")
    public ResponseEntity<Application> findOwnApplicationById(
            @PathVariable("id") String id,
            Principal principal
    ) {
        String tenantAccount = PrincipalUtil.getUserNameFrom(principal);
        Application application = applicationService.findOneOf(tenantAccount, id);
        Optional.ofNullable(application).<RecordNotFoundException>orElseThrow(() -> new RecordNotFoundException());
        return ResponseEntity.ok(application);
    }

    @Deprecated
    @RequestMapping(method = RequestMethod.POST)
    @PreAuthorize("#oauth2.clientHasAnyRole('ADMIN', 'AP_MANAGER')")
    public ResponseEntity<?> createApplication(
            @RequestParam("managerId") String managerId,
            @RequestBody Application application,
            Principal principal
            ) {
        String tenantAccount = PrincipalUtil.getUserNameFrom(principal);
        Application createdApp = applicationService.createApplication(managerId, tenantAccount, application);
        URI createdAppUri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(createdApp.getId()).toUri();
        return ResponseEntity.created(createdAppUri).body(createdApp);
    }

    @RequestMapping(value = "/own",method = RequestMethod.POST)
    @PreAuthorize("#oauth2.clientHasAnyRole('ADMIN', 'AP_MANAGER')")
    public ResponseEntity<?> createOwnApplication(
            @RequestBody Application application,
            Principal principal
    ) {
        String tenantAccount = PrincipalUtil.getUserNameFrom(principal);
        Application createdApp = applicationService.createApplication(tenantAccount, application);
        URI createdAppUri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(createdApp.getId()).toUri();
        return ResponseEntity.created(createdAppUri).body(createdApp);
    }

    @Deprecated
    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    @PreAuthorize("#oauth2.clientHasAnyRole('ADMIN', 'AP_MANAGER')")
    public ResponseEntity<?> updateApplication(
            @RequestParam("managerId") String managerId,
            @PathVariable("id") String applicationId,
            @RequestBody Application application
    ) {
        Application existingApp = applicationRepository.findByIdAndManagerId(applicationId, managerId);
        Optional.ofNullable(existingApp).<RecordNotFoundException>orElseThrow(() -> new RecordNotFoundException());
        Application createdApp = applicationRepository.save(application);
        return ResponseEntity.ok(createdApp);
    }

    @RequestMapping(value = "/own/{id}", method = RequestMethod.PUT)
    @PreAuthorize("#oauth2.clientHasAnyRole('ADMIN', 'AP_MANAGER')")
    public ResponseEntity<?> updateOwnApplication(
            @PathVariable("id") String applicationId,
            @RequestBody Application application,
            Principal principal
    ) {
        String tenantAccount = PrincipalUtil.getUserNameFrom(principal);
        Application updatedApp = applicationService.updateApplicationOf(tenantAccount, applicationId, application);
        return ResponseEntity.ok(updatedApp);
    }

    @Deprecated
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("#oauth2.clientHasAnyRole('ADMIN', 'AP_MANAGER')")
    public ResponseEntity<?> deleteApplication(
            @RequestParam("managerId") String managerId,
            @PathVariable String id
    ) {
        Application existingApp = applicationRepository.findByIdAndManagerId(id, managerId);
        Optional.ofNullable(existingApp).<RecordNotFoundException>orElseThrow(() -> new RecordNotFoundException());
        applicationRepository.delete(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/own/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("#oauth2.clientHasAnyRole('ADMIN', 'AP_MANAGER')")
    public ResponseEntity<?> deleteOwnApplication(
            @PathVariable String id,
            Principal principal
    ) {
        Application existingApp = applicationRepository.findOne(id);
        Optional.ofNullable(existingApp).<RecordNotFoundException>orElseThrow(() -> new RecordNotFoundException());
        String tenantAccount = PrincipalUtil.getUserNameFrom(principal);
        applicationService.deleteApplication(tenantAccount, id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/search/by-name-like", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('ADMIN', 'AP_MANAGER')")
    public ResponseEntity<Page<Application>> findByNameLikeAndOwnerAccount(
            @RequestParam("name") String applicationName,
            Pageable pageable,
            Principal principal
    ) {
        String tenantAccount = PrincipalUtil.getUserNameFrom(principal);

        Page<Application> applications = applicationRepository.findByNameLikeAndOwnerAccount(applicationName, tenantAccount, pageable);
        return ResponseEntity.ok(applications);
    }

    @RequestMapping(value = "/search/by-status", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('ADMIN', 'AP_MANAGER')")
    public ResponseEntity<Page<Application>> findByStatusAndOwnerAccount(
            @RequestParam("status")Application.ApplicationStatus status,
            Pageable pageable,
            Principal principal
    ) {
        String tenantAccount = PrincipalUtil.getUserNameFrom(principal);
        Page<Application> applications = applicationRepository.findByStatusAndOwnerAccount(status, tenantAccount, pageable);
        return ResponseEntity.ok(applications);
    }

    @RequestMapping(value = "/search/by-disabled-new-user", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('ADMIN', 'AP_MANAGER')")
    public ResponseEntity<Page<Application>> findByIsDisabledNewUserAndOwnerAccount(
            @RequestParam("disabledNewUser") Boolean isDisabledNewUser,
            Pageable pageable,
            Principal principal
    ) {
        String tenantAccount = PrincipalUtil.getUserNameFrom(principal);
        Page<Application> applications = applicationRepository.findByIsDisabledNewUserAndOwnerAccount(isDisabledNewUser, tenantAccount, pageable);
        return ResponseEntity.ok(applications);
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('AP_MANAGER','ADMIN')")
    public ResponseEntity<List<EndUserResource.SearchMethod>> search() {
        return ResponseEntity.ok(Arrays.asList(
                new EndUserResource.SearchMethod("by-name-like", "/api/applications/search/by-name-like")
                ,new EndUserResource.SearchMethod("by-status", "/api/applications/search/by-status")
                ,new EndUserResource.SearchMethod("by-disabled-new-user", "/api/applications/search/by-disabled-new-user")
        ));
    }
}
