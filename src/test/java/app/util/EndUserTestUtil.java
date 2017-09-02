package app.util;

import app.domain.EndUser;
import app.domain.LoginCredential;
import app.domain.UserProfile;

import javax.security.auth.Subject;
import java.security.Principal;

public class EndUserTestUtil {
    private EndUserTestUtil() {
    }

    public static EndUser createTestEndUser(String userName) {
        EndUser endUser = new EndUser();
        endUser.setEmail("test_user@test.com");
        setEndUserProfile(endUser);
        setLoginCredential(userName, endUser);
        return endUser;
    }

    public static void setEndUserProfile(EndUser endUser) {
        UserProfile profile = new UserProfile();
        profile.setNickName("nick name");
        profile.setMobilePhoneNo("010-111-2222");
        endUser.setProfile(profile);
    }

    public static void setLoginCredential(String userName, EndUser endUser) {
        LoginCredential loginCredential = new LoginCredential();
        loginCredential.setPassword("password");
        loginCredential.setAccount(userName);
        endUser.setCredential(loginCredential);
    }

    public static Principal principalForTest(final String name) {
        return new Principal(){
            @Override
            public String getName() {
                return name;
            }

            @Override
            public boolean implies(Subject subject) {
                return true;
            }
        };
    }
}