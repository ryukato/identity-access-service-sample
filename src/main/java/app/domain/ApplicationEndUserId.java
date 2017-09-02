package app.domain;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
public class ApplicationEndUserId implements Serializable {
    private EndUser endUser;
    private Application application;

    @ManyToOne
    public EndUser getEndUser() {
        return endUser;
    }

    public void setEndUser(EndUser endUser) {
        this.endUser = endUser;
    }

    @ManyToOne
    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApplicationEndUserId that = (ApplicationEndUserId) o;

        if (endUser != null ? !endUser.equals(that.endUser) : that.endUser != null) return false;
        return application != null ? application.equals(that.application) : that.application == null;

    }

    @Override
    public int hashCode() {
        int result = endUser != null ? endUser.hashCode() : 0;
        result = 31 * result + (application != null ? application.hashCode() : 0);
        return result;
    }
}
