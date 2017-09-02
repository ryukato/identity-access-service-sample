package app.resource;

import app.domain.EndUserStatus;
import app.domain.Gender;
import app.domain.Tenant;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/codes")
public class CodeEnumResource {

    @RequestMapping(value = "/gender-types", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.hasScope('read') and #oauth2.clientHasAnyRole('USER','AP_MANAGER','ADMIN')")
    public ResponseEntity<List<Map<Gender, Gender>>> allGenders() {
        List<Map<Gender, Gender>> mapList = Arrays.asList(Gender.values()).stream()
                .map(s-> {
                    Map<Gender, Gender> map = new HashMap<>();
                    map.put(s, s);
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(mapList);
    }

    @RequestMapping(value = "/end-user-status-types", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.hasScope('read') and #oauth2.clientHasAnyRole('USER','AP_MANAGER','ADMIN')")
    public ResponseEntity<List<Map<EndUserStatus, EndUserStatus>>> allEndUserStatus() {
        List<Map<EndUserStatus, EndUserStatus>> mapList = Arrays.asList(EndUserStatus.values()).stream()
                .map(s-> {
                    Map<EndUserStatus, EndUserStatus> map = new HashMap<>();
                    map.put(s, s);
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(mapList);
    }

    @RequestMapping(value = "/tenant-status-types", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.hasScope('read') and #oauth2.clientHasAnyRole('AP_MANAGER','ADMIN')")
    public ResponseEntity<List<Map<Tenant.Status, Tenant.Status>>> allTenantStatus() {
        List<Map<Tenant.Status, Tenant.Status>> mapList = Arrays.asList(Tenant.Status.values()).stream()
                .map(s-> {
                    Map<Tenant.Status, Tenant.Status> map = new HashMap<>();
                    map.put(s, s);
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(mapList);
    }





}
