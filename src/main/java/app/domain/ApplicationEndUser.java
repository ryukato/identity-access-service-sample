package app.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "APP_END_USER")
@AssociationOverrides({
        @AssociationOverride(name = "pk.application", joinColumns = @JoinColumn(name = "APP_ID"))
        , @AssociationOverride(name = "pk.endUser", joinColumns = @JoinColumn(name = "END_USER_ID"))
})
public class ApplicationEndUser extends Auditible implements Serializable {
    private ApplicationEndUserId pk = new ApplicationEndUserId();

    //for JPA
    public ApplicationEndUser() {
    }


    @EmbeddedId
    public ApplicationEndUserId getPk() {
        return pk;
    }

    public void setPk(ApplicationEndUserId pk) {
        this.pk = pk;
    }

    @Transient
    public EndUser getEndUser() {
        return pk.getEndUser();
    }

    public void setEndUser(EndUser endUser) {
        pk.setEndUser(endUser);
    }

    public void setApplication(Application application) {
        pk.setApplication(application);
    }

    @Transient
    public Application getApplication() {
        return pk.getApplication();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApplicationEndUser that = (ApplicationEndUser) o;

        return pk != null ? pk.equals(that.pk) : that.pk == null;

    }

    @Override
    public int hashCode() {
        return pk != null ? pk.hashCode() : 0;
    }
}
