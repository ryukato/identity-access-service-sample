package app.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/*
 // TODO : EndUesr 하위의 username은 credential의 account와 중복 따라서 제거, credential account로 사용자 조회 api 추가 필요.
 * DomainUserDetailsService
 * EndUserRepository
 * EndUserResource
 *
 * EndUser의 중복체크는 Ap Manager(Tenant) 별로 제한한다.
 */

@Entity(name = "APP_USER")
public class EndUser extends Auditible {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "VARCHAR(36)", name = "END_USER_ID")
    private String id;

    @Email
    public String email;

    @JsonDeserialize(using = EndUserStatusDeserializer.class)
    @Enumerated(EnumType.STRING)
    private EndUserStatus status;

    @Embedded
    private LoginCredential credential;

    private UserProfile profile;

    @JsonProperty("applications")
    @JsonSerialize(using = EndUserApplicationJsonSerializer.class)
    @JsonDeserialize(using = EndUserApplicationJsonDeserializer.class)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pk.endUser", cascade = CascadeType.ALL)
    private List<ApplicationEndUser> applicationEndUsers = new ArrayList<ApplicationEndUser>();

    @JsonIgnore
    @org.codehaus.jackson.annotate.JsonIgnore
    private String tenantId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public EndUserStatus getStatus() {
        return status;
    }

    public void setStatus(EndUserStatus status) {
        this.status = status;
    }

    public LoginCredential getCredential() {
        return credential;
    }

    public void setCredential(LoginCredential credential) {
        this.credential = credential;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    public List<ApplicationEndUser> getApplicationEndUsers() {
        return Optional.ofNullable(applicationEndUsers).orElse(new ArrayList<>());
    }

    public void setApplicationEndUsers(List<ApplicationEndUser> applicationEndUsers) {
        this.applicationEndUsers = applicationEndUsers;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EndUser endUser = (EndUser) o;

        return id != null ? id.equals(endUser.id) : endUser.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "EndUser{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                ", profile=" + profile +
                ", profile=" + profile +
                '}';
    }
}
