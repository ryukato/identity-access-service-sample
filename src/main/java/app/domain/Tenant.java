package app.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Tenant extends Auditible {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;

    @Email
    private String email;

    @Embedded
    private LoginCredential loginCredential;

    private String companyName;

    // TODO: deserialize
    @JsonDeserialize(using = StatusDeserializer.class)
    private Status status;

    private UserProfile profile;

    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL)
    private ApiKeyInformation apiKeyInformation;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private Set<Application> applications = new HashSet<>();


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

    public LoginCredential getLoginCredential() {
        return loginCredential;
    }

    public void setLoginCredential(LoginCredential loginCredential) {
        this.loginCredential = loginCredential;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    public ApiKeyInformation getApiKeyInformation() {
        return apiKeyInformation;
    }

    public void setApiKeyInformation(ApiKeyInformation apiKeyInformation) {
        this.apiKeyInformation = apiKeyInformation;
    }

    public Set<Application> getApplications() {
        return applications;
    }

    public void setApplications(Set<Application> applications) {
        this.applications = applications;
    }

    public enum Status {
        CREATED, ACTIVE, INACTIVE, LOCKED, TERMINATED, UNKNOWN;

        public static Status fromString(String value) {
            if (value == null || value.isEmpty()) {
                return UNKNOWN;
            }
            return Arrays.stream(values())
                    .filter(g -> g.name().equalsIgnoreCase(value))
                    .findFirst()
                    .orElse(UNKNOWN);
        }
    }

    public static class StatusDeserializer extends JsonDeserializer<Status> {
        @Override
        public Status deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            return Status.fromString(jsonParser.getValueAsString());
        }
    }
}
