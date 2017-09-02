package app.resource;

import app.domain.Application;
import app.domain.EndUser;
import app.domain.PasswordUpdateRequest;
import app.domain.Tenant;
import app.error.ErrorResponse;
import app.repository.TenantRepository;
import app.service.TenantService;
import app.util.PrincipalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.security.Principal;
import java.util.Collection;

@RestController
@RequestMapping("/api/tenants")
public class TenantResource {
    private static final String SYS_ADMIN_AUTHORITY = "SYS_ADMIN";

    private final TenantRepository tenantRepository;

    private final TenantService tenantService;

    @Autowired
    public TenantResource(TenantRepository tenantRepository, TenantService tenantService) {
        this.tenantRepository = tenantRepository;
        this.tenantService = tenantService;
    }

    @RequestMapping(method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('SYS_ADMIN')")
    public ResponseEntity<Page<Tenant>> findAll(Pageable pageable) {
        return ResponseEntity.ok(tenantRepository.findAll(pageable));
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<Tenant> register(@RequestBody Tenant tenant) {
        Tenant createdTenant = tenantService.createTenant(tenant);
        return ResponseEntity.ok(createdTenant);
    }

    @RequestMapping(value = "/{id}/activation", method = RequestMethod.PUT)
    @PreAuthorize("#oauth2.hasScope('write') and #oauth2.clientHasAnyRole('AP_MANAGER','ADMIN', 'SYS_ADMIN')")
    public ResponseEntity<Tenant> activate(@PathVariable String id) {
        Tenant tenant = tenantService.activate(id);
        // TODO : notify Tenant activated Event
        return ResponseEntity.ok(tenant);
    }

    @RequestMapping(value = "/{id}/inactivation", method = RequestMethod.PUT)
    @PreAuthorize("#oauth2.hasScope('write') and #oauth2.clientHasAnyRole('AP_MANAGER','ADMIN', 'SYS_ADMIN')")
    public ResponseEntity<Tenant> inactivate(@PathVariable String id) {
        Tenant tenant = tenantService.inactivate(id);
        // TODO : notify Tenant activated Event
        return ResponseEntity.ok(tenant);
    }

    @RequestMapping(value = "/{id}/lock", method = RequestMethod.PUT)
    @PreAuthorize("#oauth2.hasScope('write') and #oauth2.clientHasAnyRole('AP_MANAGER','ADMIN', 'SYS_ADMIN')")
    public ResponseEntity<Tenant> lock(@PathVariable String id) {
        Tenant tenant = tenantService.lock(id);
        // TODO : notify Tenant activated Event
        return ResponseEntity.ok(tenant);
    }

    @RequestMapping(value = "/{id}/termination", method = RequestMethod.PUT)
    @PreAuthorize("#oauth2.hasScope('write') and #oauth2.clientHasAnyRole('AP_MANAGER','ADMIN', 'SYS_ADMIN')")
    public ResponseEntity<Tenant> terminate(@PathVariable String id) {
        Tenant tenant = tenantService.terminate(id);
        // TODO : notify Tenant activated Event
        return ResponseEntity.ok(tenant);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @PreAuthorize("#oauth2.hasScope('write') and #oauth2.clientHasAnyRole('AP_MANAGER','ADMIN', 'SYS_ADMIN')")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Tenant tenant, Principal principal) {
        if (isAllowedEdit(principal, id)) {
            return ResponseEntity.ok(tenantService.update(id, tenant));
        } else {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    "Not Allowed tenant update",
                    "No Authorities",
                    ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").toUriString(),
                    HttpStatus.BAD_REQUEST
            ));
        }
    }

    @RequestMapping(value = "/{id}/password", method = RequestMethod.PUT)
    @PreAuthorize("#oauth2.hasScope('write') and #oauth2.clientHasAnyRole('AP_MANAGER','ADMIN', 'SYS_ADMIN')")
    public ResponseEntity<?> updatePassword(
            @PathVariable String id,
            @RequestBody PasswordUpdateRequest passwordUpdateRequest,
            Principal principal) {
        if (isAllowedEdit(principal, id)) {
            Tenant tenant = tenantService.updatePassword(id, passwordUpdateRequest);
            return ResponseEntity.ok(tenant);
        } else {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    "Not Allowed update tenant password",
                    "No Authorities",
                    ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").toUriString(),
                    HttpStatus.BAD_REQUEST
            ));
        }
    }

    @RequestMapping(value = "/{id}/applications", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.hasScope('write') and #oauth2.clientHasAnyRole('AP_MANAGER','ADMIN', 'SYS_ADMIN')")
    public ResponseEntity<Page<Application>> findAllApplications(@PathVariable String id, Pageable pageable) {
        return ResponseEntity.ok(tenantRepository.findApplicationsByTenantId(id, pageable));
    }

    @RequestMapping(value = "/{id}/end-users", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.hasScope('write') and #oauth2.clientHasAnyRole('AP_MANAGER','ADMIN', 'SYS_ADMIN')")
    public ResponseEntity<Page<EndUser>> findAllEndUsers(@PathVariable String id, Pageable pageable) {
        return ResponseEntity.ok(tenantRepository.findEndUsersByTenantId(id, pageable));
    }

    private boolean isAllowedEdit(Principal principal, String id) {
        Tenant tenant = tenantRepository.findById(id);
        String account = PrincipalUtil.getUserNameFrom(principal);
        Collection<? extends GrantedAuthority> grantedAuthorities = PrincipalUtil.getUserAuthorities(principal);
        return (tenant != null && account.equals(tenant.getLoginCredential().getAccount())) ||
                (grantedAuthorities != null && grantedAuthorities.contains(new SimpleGrantedAuthority(SYS_ADMIN_AUTHORITY)));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("#oauth2.clientHasAnyRole('SYS_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        tenantService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.hasScope('read') and #oauth2.clientHasAnyRole('AP_MANAGER','ADMIN', 'SYS_ADMIN')")
    public ResponseEntity<Tenant> findById(@PathVariable String id) {
        return ResponseEntity.ok(tenantRepository.findById(id));
    }

    @RequestMapping(value = "/search/by-email", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.hasScope('read') and #oauth2.clientHasAnyRole('AP_MANAGER','ADMIN', 'SYS_ADMIN')")
    public ResponseEntity<Tenant> findByEmail(@RequestParam String email) {
        return ResponseEntity.ok(tenantRepository.findByEmail(email));
    }

    @RequestMapping(value = "/search/by-account", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.hasScope('read') and #oauth2.clientHasAnyRole('AP_MANAGER','ADMIN', 'SYS_ADMIN')")
    public ResponseEntity<Tenant> findByAccount(@RequestParam String account) {
        return ResponseEntity.ok(tenantRepository.findByLoginCredentialAccount(account));
    }

    @RequestMapping(value = "/search/by-api-key", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.hasScope('read') and #oauth2.clientHasAnyRole('AP_MANAGER','ADMIN', 'SYS_ADMIN')")
    public ResponseEntity<Tenant> findByApiKey(@RequestParam String apiKey) {
        return ResponseEntity.ok(tenantRepository.findByApiKeyInformationApiKey(apiKey));
    }


}
