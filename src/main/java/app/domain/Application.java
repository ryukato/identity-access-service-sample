package app.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity(name = "TENANT_APPLICATION")
public class Application extends Auditible {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "VARCHAR(36)", name = "APP_ID")
    private String id;;

    private String managerId;

    private String apiKey;
    private String name;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Column(name = "DISABLE_NEW_USER")
    private boolean isDisabledNewUser;

    @Transient
    private List<String> authorizedGrantTypes = new ArrayList<>();

    @Transient
    private List<GrantedAuthority> authorities = new ArrayList<>();

    @Transient
    private Set<String> registeredRedirectUris = new HashSet<>();

    @Transient
    private List<String> scopes = new ArrayList<>();

    @JsonSerialize(using = ApplicationOwnerSerializer.class)
    @JsonDeserialize(using = ApplicationOwnerDeSerializer.class)
    @ManyToOne(fetch = FetchType.LAZY)
    private Tenant owner;

    @org.codehaus.jackson.annotate.JsonIgnore
    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pk.application", cascade = CascadeType.ALL)
    private Set<ApplicationEndUser> applicationEndUsers = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public boolean isDisabledNewUser() {
        return isDisabledNewUser;
    }

    public void setDisabledNewUser(boolean disabledNewUser) {
        isDisabledNewUser = disabledNewUser;
    }

    public Set<ApplicationEndUser> getApplicationEndUsers() {
        return applicationEndUsers;
    }

    public void setApplicationEndUsers(Set<ApplicationEndUser> applicationEndUsers) {
        this.applicationEndUsers = applicationEndUsers;
    }

    public List<String> getAuthorizedGrantTypes() {
        return authorizedGrantTypes;
    }

    public void setAuthorizedGrantTypes(List<String> authorizedGrantTypes) {
        this.authorizedGrantTypes = authorizedGrantTypes;
    }

    public List<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public Set<String> getRegisteredRedirectUris() {
        return registeredRedirectUris;
    }

    public void setRegisteredRedirectUris(Set<String> registeredRedirectUris) {
        this.registeredRedirectUris = registeredRedirectUris;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public Tenant getOwner() {
        return owner;
    }

    public void setOwner(Tenant owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Application that = (Application) o;

        return id != null ? id.equals(that.id) : that.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Application{" +
                "id='" + id + '\'' +
                ", managerId='" + managerId + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", isDisabledNewUser=" + isDisabledNewUser +
                ", authorizedGrantTypes=" + authorizedGrantTypes +
                ", authorities=" + authorities +
                ", registeredRedirectUris=" + registeredRedirectUris +
                ", scopes=" + scopes +
                ", applicationEndUsers=" + applicationEndUsers +
                '}';
    }

    public enum ApplicationStatus{
        CREATED, ACTIVE, SUSPENDED, TERMINATED;
    }
}
