package app.resource;

import app.domain.EndUser;
import app.domain.Gender;
import app.domain.PasswordUpdateRequest;
import app.domain.Tenant;
import app.error.ErrorResponse;
import app.error.InvalidTenantAccountException;
import app.error.RecordNotFoundException;
import app.repository.EndUserRepository;
import app.repository.TenantRepository;
import app.service.EndUserService;
import app.util.PrincipalUtil;
import io.swagger.annotations.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.Serializable;
import java.net.URI;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/end-users")
@Api(value = "EndUser들을 조회, 수정, 삭제 할 수 있는 API를 제공하며, 조회 시 기본적으로 현재의 Tenant(AP Manager)가 관리하는 End-user들을 조회한다.",
        authorizations = {
                @Authorization(value = "oauth2")
        }
)
public class EndUserResource {
    private static final String AP_MANAGER_AUTHORITY = "AP_MANAGER";

    private final EndUserRepository endUserRepository;

    private final TenantRepository tenantRepository;

    private final EndUserService endUserService;

    public EndUserResource(EndUserRepository endUserRepository, TenantRepository tenantRepository, EndUserService endUserService) {
        this.endUserRepository = endUserRepository;
        this.tenantRepository = tenantRepository;
        this.endUserService = endUserService;
    }

    @ApiOperation(value = "Tenant별 모든 End-user 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "page 번호", example = "0", dataType = "Integer" ,defaultValue = "0"),
            @ApiImplicitParam(name = "size", value = "page 게시물 개수", example = "10", dataType = "Integer" ,defaultValue = "20"),
            @ApiImplicitParam(name = "sort", value = "page 게시물 정렬", example = "id,asc", dataType = "String")
    })
    @RequestMapping(method = RequestMethod.GET)
    @PreAuthorize("#oauth2.hasScope('read') and #oauth2.clientHasAnyRole('AP_MANAGER', 'ADMIN')")
    public ResponseEntity<Page<EndUser>> findAll(Principal principal, Pageable pageable) {
        Tenant tenant = getTenant(principal);
        return ResponseEntity.ok(endUserRepository.findAllOfTenant(tenant.getId(), pageable));
    }

    @ApiOperation(value = "Tenant별 모든 End-user를 Status별 개수 조희")
    @RequestMapping(value = "/count-by-status", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.hasScope('read') and #oauth2.clientHasAnyRole('AP_MANAGER', 'ADMIN')")
    public ResponseEntity<List<Object[]>> findCountsEndUserByStatus(Principal principal) {
        Tenant tenant = getTenant(principal);
        return ResponseEntity.ok(endUserRepository.findCountsEndUserByStatusForTenant(tenant.getId()));
    }

    @ApiOperation(value = "단일 End-user 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "End-user ID", example = "ed203469-2d21-447e-994a-04821a05e5e4", dataType = "String", paramType = "path")
    })
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.hasScope('read') and #oauth2.clientHasAnyRole('USER','AP_MANAGER','ADMIN')")
    public ResponseEntity<EndUser> findById(@PathVariable String id) {
        EndUser endUser = endUserRepository.findById(id);
        return ResponseEntity.ok(Optional.ofNullable(endUser).<RecordNotFoundException>orElseThrow(RecordNotFoundException::new));
    }

    @ApiOperation(value = "삭제 예정")
    @Deprecated
    @RequestMapping(value = "/by-tenant", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.hasScope('read') and #oauth2.clientHasAnyRole('AP_MANAGER', 'ADMIN')")
    public ResponseEntity<Page<EndUser>> findEndUserByTenant(Pageable pageable, Principal principal) {
        Tenant tenant = getTenant(principal);
        Optional.ofNullable(tenant).<RecordNotFoundException>orElseThrow(() -> new RecordNotFoundException("Invalid Tenant"));
        String tenantId = tenant.getId();
        return ResponseEntity.ok(endUserRepository.findAllEndUsersForTenant(tenantId, pageable));
    }


    @RequestMapping(method = RequestMethod.POST)
    @PreAuthorize("#oauth2.hasScope('write') and #oauth2.clientHasAnyRole('AP_MANAGER','ADMIN')")
    public ResponseEntity<?> createEndUser(@RequestParam String applicationId, @RequestBody EndUser endUser) {
        EndUser createdEndUser = endUserService.createEndUser(applicationId, endUser);
        URI createdUserUri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(createdEndUser.getId()).toUri();
        return ResponseEntity.created(createdUserUri).body(createdEndUser);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @PreAuthorize("#oauth2.hasScope('write') and #oauth2.clientHasAnyRole('USER','AP_MANAGER','ADMIN')")
    public ResponseEntity<?> updateEndUser(
            @PathVariable String id,
            @RequestParam(value = "applicationId", required = false) String applicationId,
            @RequestBody EndUser endUser,
            Principal principal) {

        if (isAllowedEdit(principal, id)) {
            EndUser updatedEndUser = endUserService.updateEndUser(id, endUser);
            return ResponseEntity.ok(updatedEndUser);
        } else {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    "Not Allowed user update",
                    "No Authorities",
                    ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").toUriString(),
                    HttpStatus.BAD_REQUEST
            ));
        }
    }

    private boolean isAllowedEdit(Principal principal, String id) {
        EndUser endUser = endUserRepository.findById(id);
        String account = PrincipalUtil.getUserNameFrom(principal);
        Collection<? extends GrantedAuthority> grantedAuthorities = PrincipalUtil.getUserAuthorities(principal);
        return (endUser != null && account.equals(endUser.getCredential().getAccount())) ||
                (grantedAuthorities != null && grantedAuthorities.contains(new SimpleGrantedAuthority(AP_MANAGER_AUTHORITY)));
    }

    @RequestMapping(value = "/{id}/password", method = RequestMethod.PUT)
    @PreAuthorize("#oauth2.hasScope('write') and #oauth2.clientHasAnyRole('USER','AP_MANAGER','ADMIN')")
    public ResponseEntity<?> updateEndUserPassword(
            @PathVariable String id,
            @RequestParam("applicationId") String applicationId,
            @RequestBody PasswordUpdateRequest passwordUpdateRequest,
            Principal principal) {

        String userName = PrincipalUtil.getUserNameFrom(principal);

        EndUser endUser = endUserRepository.findByApplicationAndCredentialAccount(applicationId, userName);
        if (endUser != null && id.equals(endUser.getId())) {
            EndUser updatedEndUser = endUserService.updatePassword(id, passwordUpdateRequest);
            return ResponseEntity.ok(updatedEndUser);
        }
        return ResponseEntity.badRequest().body(new ErrorResponse(
                "Not Allowed Password update",
                "Not credential owner",
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").toUriString(),
                HttpStatus.BAD_REQUEST
        ));
    }

    @RequestMapping(value = "/{id}/profile", method = RequestMethod.PUT)
    @PreAuthorize("#oauth2.hasScope('write') and #oauth2.clientHasAnyRole('USER','AP_MANAGER','ADMIN')")
    public ResponseEntity<?> updateEndUserProfile(
            @PathVariable String id,
            @RequestParam(value = "applicationId", required = false) String applicationId,
            @RequestBody EndUser endUser,
            Principal principal) {
        if (isAllowedEdit(principal, id)) {
            EndUser profileUpdatedEndUser = endUserService.updateEndUserProfile(id, endUser);
            return ResponseEntity.ok(profileUpdatedEndUser);
        } else {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    "Not Allowed user update",
                    "No Authorities",
                    ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").toUriString(),
                    HttpStatus.BAD_REQUEST
            ));
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("#oauth2.clientHasAnyRole('AP_MANAGER','ADMIN')")
    public ResponseEntity<?> deleteEndUser(@RequestParam String applicationId, @PathVariable String id) {
        endUserService.deleteEndUser(applicationId, id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/{id}/un-registration", method = RequestMethod.PUT)
    @PreAuthorize("#oauth2.clientHasAnyRole('AP_MANAGER','ADMIN')")
    public ResponseEntity<?> unregisterEndUser(
            @RequestParam("applicationId") String applicationId,
            @PathVariable String id) {
        EndUser unregisteredEndUser = endUserService.unregisterEndUser(applicationId, id);
        return ResponseEntity.ok(unregisteredEndUser);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<?> register(@RequestParam String applicationId, @RequestBody EndUser endUser) {
        EndUser registeredEndUser = endUserService.register(applicationId, endUser);
        URI createdUserUri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(registeredEndUser.getId()).toUri();
        return ResponseEntity.created(createdUserUri).body(registeredEndUser);
    }

    @RequestMapping(value = "/{id}/activation", method = RequestMethod.PUT)
    public ResponseEntity<?> activate(@RequestParam String applicationId, @PathVariable("id") String id) {
        EndUser activatedEndUser = endUserService.activate(applicationId, id);
        return ResponseEntity.ok(activatedEndUser);
    }

    @RequestMapping(value = "/{id}/termination", method = RequestMethod.PUT)
    public ResponseEntity<?> terminate(@RequestParam String applicationId, @PathVariable("id") String id) {
        EndUser terminatedEndUser = endUserService.terminate(applicationId, id);
        return ResponseEntity.ok(terminatedEndUser);
    }

    @RequestMapping(value = "/{id}/suspend", method = RequestMethod.PUT)
    public ResponseEntity<?> suspend(@RequestParam String applicationId, @PathVariable("id") String id) {
        EndUser suspendedEndUser = endUserService.suspend(applicationId, id);
        return ResponseEntity.ok(suspendedEndUser);
    }

    @ApiOperation(value = "현재의 Tenant 및 특정 Application내의 End-user중 이름의 일부 혹은 전체가 일치하는 End-user 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "Application ID", example = "ed203469-2d21-447e-994a-04821a05e5e4", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "firstName", value = "사용자 이름(성씨 제)", example = "길동", dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/search/by-firstName-like", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('AP_MANAGER','ADMIN')")
    public ResponseEntity<Page<EndUser>> findByFirstNameLike(
            @RequestParam(value = "applicationId", required = false) String applicationId,
            @RequestParam("firstName") String firstName,
            Principal principal,
            Pageable pageable) {
        Tenant tenant = getTenant(principal);
        Page<EndUser> endUsers;
        if (applicationId != null && !applicationId.isEmpty()) {
            endUsers = endUserRepository.findByProfileFirstNameLike(firstName, applicationId, tenant.getId(), pageable);
        } else {
            endUsers = endUserRepository.findByProfileFirstNameLike(firstName, tenant.getId(), pageable);
        }
        return ResponseEntity.ok(endUsers);
    }

    private Tenant getTenant(Principal principal) {
        String tenantAccount = PrincipalUtil.getUserNameFrom(principal);
        Tenant tenant = tenantRepository.findByLoginCredentialAccount(tenantAccount);

        return Optional.ofNullable(tenant)
                .<InvalidTenantAccountException>orElseThrow(() -> new InvalidTenantAccountException(String.format("Invalid tenant account(%s)", tenantAccount)));
    }

    @ApiOperation(value = "현재의 Tenant 및 특정 Application내의 End-user중 이름이 일치하는 End-user를  조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "Application ID", example = "ed203469-2d21-447e-994a-04821a05e5e4", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "firstName", value = "사용자 이름(성씨 제)", example = "길동", dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/search/by-firstName", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('AP_MANAGER','ADMIN')")
    public ResponseEntity<Page<EndUser>> findByFirstName(
            @RequestParam(value = "applicationId", required = false) String applicationId,
            @RequestParam("firstName") String firstName,
            Principal principal,
            Pageable pageable) {
        Tenant tenant = getTenant(principal);

        Page<EndUser> endUsers;
        if (applicationId != null && !applicationId.isEmpty()) {
            endUsers = endUserRepository.findByProfileFirstName(firstName, applicationId, tenant.getId(), pageable);
        } else {
            endUsers = endUserRepository.findByProfileFirstName(firstName, tenant.getId(), pageable);
        }

        return ResponseEntity.ok(endUsers);
    }

    @ApiOperation(value = "현재의 Tenant 및 특정 Application내의 End-user중 성의 일부 혹은 전체가 일치하는 End-user를  조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "Application ID", example = "ed203469-2d21-447e-994a-04821a05e5e4", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "lastName", value = "사용자 성", example = "홍", dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/search/by-lastName-like", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('AP_MANAGER','ADMIN')")
    public ResponseEntity<Page<EndUser>> findByLastNameLike(
            @RequestParam(value = "applicationId", required = false) String applicationId,
            @RequestParam("lastName") String lastName,
            Principal principal,
            Pageable pageable) {
        Tenant tenant = getTenant(principal);
        Page<EndUser> endUsers;
        if (applicationId != null && !applicationId.isEmpty()) {
            endUsers = endUserRepository.findByProfileLastNameLike(lastName, applicationId, tenant.getId(), pageable);
        } else {
            endUsers = endUserRepository.findByProfileLastNameLike(lastName, tenant.getId(), pageable);
        }
        return ResponseEntity.ok(endUsers);
    }

    @ApiOperation(value = "현재의 Tenant 및 특정 Application내의 End-user중 성이 일치하는 End-user를  조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "Application ID", example = "ed203469-2d21-447e-994a-04821a05e5e4", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "lastName", value = "사용자 성", example = "홍", dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/search/by-lastName", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('AP_MANAGER','ADMIN')")
    public ResponseEntity<Page<EndUser>> findByLastName(
            @RequestParam(value = "applicationId", required = false) String applicationId,
            @RequestParam("lastName") String lastName,
            Principal principal,
            Pageable pageable) {
        Tenant tenant = getTenant(principal);
        Page<EndUser> endUsers;
        if (applicationId != null && !applicationId.isEmpty()) {
            endUsers = endUserRepository.findByProfileLastName(lastName, applicationId, tenant.getId(), pageable);
        } else {
            endUsers = endUserRepository.findByProfileLastName(lastName, tenant.getId(), pageable);
        }
        return ResponseEntity.ok(endUsers);
    }

    @ApiOperation(value = "현재의 Tenant 및 특정 Application내의 End-user중 중간 이름의 일부 혹은 전체가 일치하는 End-user를  조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "Application ID", example = "ed203469-2d21-447e-994a-04821a05e5e4", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "middleName", value = "중간 이름", example = "", dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/search/by-middleName-like", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('AP_MANAGER','ADMIN')")
    public ResponseEntity<Page<EndUser>> findByMiddleNameLike(
            @RequestParam(value = "applicationId", required = false) String applicationId,
            @RequestParam("middleName") String middleName,
            Principal principal,
            Pageable pageable) {
        Tenant tenant = getTenant(principal);
        Page<EndUser> endUsers;
        if (applicationId != null && !applicationId.isEmpty()) {
            endUsers = endUserRepository.findByProfileMiddleNameLike(middleName, applicationId, tenant.getId(), pageable);
        } else {
            endUsers = endUserRepository.findByProfileMiddleNameLike(middleName, tenant.getId(), pageable);
        }
        return ResponseEntity.ok(endUsers);
    }

    @ApiOperation(value = "현재의 Tenant 및 특정 Application내의 End-user중 중간 이름이 일치하는 End-user를  조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "Application ID", example = "ed203469-2d21-447e-994a-04821a05e5e4", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "middleName", value = "중간 이름", example = "", dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/search/by-middleName", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('AP_MANAGER','ADMIN')")
    public ResponseEntity<Page<EndUser>> findByMiddleName(
            @RequestParam(value = "applicationId", required = false) String applicationId,
            @RequestParam("middleName") String middleName,
            Principal principal,
            Pageable pageable) {
        Tenant tenant = getTenant(principal);
        Page<EndUser> endUsers;
        if (applicationId != null && !applicationId.isEmpty()) {
            endUsers = endUserRepository.findByProfileMiddleName(middleName, applicationId, tenant.getId(), pageable);
        } else {
            endUsers = endUserRepository.findByProfileMiddleName(middleName, tenant.getId(), pageable);
        }
        return ResponseEntity.ok(endUsers);
    }

    // TODO: search end user within a current tenant
    @ApiOperation(value = "현재의 Tenant 및 특정 Application내의 End-user중 이메일의 일부 혹은 전체가 일치하는 End-user를  조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "Application ID", example = "ed203469-2d21-447e-994a-04821a05e5e4", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "email", value = "이메일 주소", example = "", dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/search/by-email-like", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('AP_MANAGER','ADMIN')")
    public ResponseEntity<Page<EndUser>> findByEmailLike(
            @RequestParam(value = "applicationId", required = false) String applicationId,
            @RequestParam("email") String email,
            Principal principal,
            Pageable pageable) {
        Tenant tenant = getTenant(principal);
        Page<EndUser> endUsers;
        if (applicationId != null && !applicationId.isEmpty()) {
            endUsers = endUserRepository.findByEmailLike(email, applicationId, tenant.getId(), pageable);
        } else {
            endUsers = endUserRepository.findByEmailLike(email, tenant.getId(), pageable);
        }
        return ResponseEntity.ok(endUsers);
    }

    // TODO: search end user within a current tenant
    @ApiOperation(value = "현재의 Tenant 및 특정 Application내의 End-user중 이메일이 일치하는 End-user를  조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "Application ID", example = "ed203469-2d21-447e-994a-04821a05e5e4", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "email", value = "이메일 주소", example = "", dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/search/by-email", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('USER','AP_MANAGER','ADMIN')")
    public ResponseEntity<EndUser> findByEmail(
            @RequestParam(value = "applicationId", required = false) String applicationId,
            @RequestParam("email") String email,
            Principal principal) {
        Tenant tenant = getTenant(principal);
        EndUser endUser;
        if (applicationId != null && !applicationId.isEmpty()) {
            endUser = endUserRepository.findByEmail(email, applicationId, tenant.getId());
        } else {
            endUser = endUserRepository.findByEmail(email, tenant.getId());
        }
        return ResponseEntity.ok(endUser);
    }

    @ApiOperation(value = "현재의 Tenant 및 특정 Application내의 End-user중 핸드폰 번호가 일치하는 End-user를  조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "Application ID", example = "ed203469-2d21-447e-994a-04821a05e5e4", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "mobileNo", value = "핸드폰 번호", example = "", dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/search/by-mobileNo", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('AP_MANAGER','ADMIN')")
    public ResponseEntity<EndUser> findByMobileNo(
            @RequestParam(value = "applicationId", required = false) String applicationId,
            @RequestParam("mobileNo") String mobileNo,
            Principal principal,
            Pageable pageable) {
        Tenant tenant = getTenant(principal);
        EndUser endUser;
        if (applicationId != null && !applicationId.isEmpty()) {
            endUser = endUserRepository.findByProfileMobilePhoneNo(mobileNo, applicationId, tenant.getId());
        } else {
            endUser = endUserRepository.findByProfileMobilePhoneNo(mobileNo, tenant.getId());
        }
        return ResponseEntity.ok(endUser);
    }

    @ApiOperation(value = "현재의 Tenant 및 특정 Application내의 End-user중 성별이 일치하는 End-user를  조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "Application ID", example = "ed203469-2d21-447e-994a-04821a05e5e4", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "gender", value = "성별(남: MALE, 여: FEMALE, 없음: NONE", example = "", dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/search/by-gender", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('AP_MANAGER','ADMIN')")
    public ResponseEntity<Page<EndUser>> findByGender(
            @RequestParam(value = "applicationId", required = false) String applicationId,
            @RequestParam("gender") Gender gender,
            Principal principal,
            Pageable pageable) {
        Tenant tenant = getTenant(principal);
        Page<EndUser> endUsers;
        if (applicationId != null && !applicationId.isEmpty()) {
            endUsers = endUserRepository.findByProfileGender(gender, applicationId, tenant.getId(), pageable);
        } else {
            endUsers = endUserRepository.findByProfileGender(gender, tenant.getId(), pageable);
        }
        return ResponseEntity.ok(endUsers);
    }

    @ApiOperation(value = "현재의 Tenant 및 특정 Application내의 End-user중 별명이 일치하는 End-user를  조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "Application ID", example = "ed203469-2d21-447e-994a-04821a05e5e4", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "nickname", value = "별명", example = "길동이", dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/search/by-nickname", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('AP_MANAGER','ADMIN')")
    public ResponseEntity<Page<EndUser>> findByNickName(
            @RequestParam(value = "applicationId", required = false) String applicationId,
            @RequestParam("nickname") String nickname,
            Principal principal,
            Pageable pageable) {
        Tenant tenant = getTenant(principal);
        Page<EndUser> endUsers;
        if (applicationId != null && !applicationId.isEmpty()) {
            endUsers = endUserRepository.findByProfileNickName(nickname, applicationId, tenant.getId(), pageable);
        } else {
            endUsers = endUserRepository.findByProfileNickName(nickname, tenant.getId(), pageable);
        }
        return ResponseEntity.ok(endUsers);
    }

    @ApiOperation(value = "현재의 Tenant 및 특정 Application내의 End-user중 별명의 일부 혹은 전체가 일치하는 End-user를  조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "Application ID", example = "ed203469-2d21-447e-994a-04821a05e5e4", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "nickname", value = "별명", example = "길동이", dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "/search/by-nickname-like", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('AP_MANAGER','ADMIN')")
    public ResponseEntity<Page<EndUser>> findByNickNameLike(
            @RequestParam(value = "applicationId", required = false) String applicationId,
            @RequestParam("nickname") String nickname,
            Principal principal,
            Pageable pageable) {
        Tenant tenant = getTenant(principal);
        Page<EndUser> endUsers;
        if (applicationId != null && !applicationId.isEmpty()) {
            endUsers = endUserRepository.findByProfileNickNameLike(nickname, applicationId, tenant.getId(), pageable);
        } else {
            endUsers = endUserRepository.findByProfileNickNameLike(nickname, tenant.getId(), pageable);
        }
        return ResponseEntity.ok(endUsers);
    }

    @ApiOperation(value = "현재의 Tenant 및 특정 Application내의 End-user를 조회할 수 있는 API path를 반환")
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.clientHasAnyRole('AP_MANAGER','ADMIN')")
    public ResponseEntity<List<SearchMethod>> search() {
        return ResponseEntity.ok(Arrays.asList(
                 new SearchMethod("by-firstName-like", "/api/end-users/search/by-firstName-like")
                ,new SearchMethod("by-firstName-like", "/api/end-users/search/by-firstName-like")
                ,new SearchMethod("by-firstName", "/api/end-users/search/by-firstName")
                ,new SearchMethod("by-lastName-like", "/api/end-users/search/by-lastName-like")
                ,new SearchMethod("by-lastName", "/api/end-users/search/by-lastName")
                ,new SearchMethod("by-middleName-like", "/api/end-users/search/by-middleName-like")
                ,new SearchMethod("by-middleName", "/api/end-users/search/by-middleName")
                ,new SearchMethod("by-email-like", "/api/end-users/search/by-email-like")
                ,new SearchMethod("by-email", "/api/end-users/search/by-email")
                ,new SearchMethod("by-mobileNo", "/api/end-users/search/by-mobileNo")
                ,new SearchMethod("by-gender", "/api/end-users/search/by-gender")
                ,new SearchMethod("by-nickname", "/api/end-users/search/by-nickname")
                ,new SearchMethod("by-nickname-like", "/api/end-users/search/by-nickname-like")
        ));
    }

    public static class SearchMethod implements Serializable{
        private final String method;
        private final String apiPath;

        public SearchMethod(String method, String apiPath) {
            this.method = method;
            this.apiPath = apiPath;
        }

        public String getMethod() {
            return method;
        }
        public String getApiPath() {
            return apiPath;
        }
    }
}