package app.resource;

import app.domain.*;
import app.repository.ApplicationRepository;
import app.repository.EndUserRepository;
import app.util.EndUserTestUtil;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class EndUserResourceTestUtil {
    private EndUserResourceTestUtil() {}
    public static Application prepareTestApplications(ApplicationRepository applicationRepository) {
        return prepareTestApplications(applicationRepository, Collections.emptyList());
    }

    public static Application prepareTestApplications(ApplicationRepository applicationRepository, List<GrantedAuthority> grantedAuthorities) {
        try {
            Application application = createTestApplication(grantedAuthorities);
            return applicationRepository.save(application);
        }catch (Throwable t) {
            System.err.println(t.getMessage());
            throw new RuntimeException(t);
        }
    }

    public static Application createTestApplication(List<GrantedAuthority> grantedAuthorities) {
        Application application = new Application();
        application.setManagerId("test_manager");
        application.setName("test_application");
        application.setStatus(Application.ApplicationStatus.ACTIVE);
        application.setDisabledNewUser(false);
        application.setApiKey("test_api_key");
        application.setAuthorities(grantedAuthorities);
        return application;
    }

    public static Application createTestApplication() {
       return createTestApplication(Collections.emptyList());
    }

    public static EndUser prepareTestUsers(PasswordEncoder passwordEncoder, EndUserRepository endUserRepository) {
        try {
            EndUser endUser = new EndUser();
            endUser.setStatus(EndUserStatus.CREATED);
            endUser.setEmail("test_user@test.com");
            LoginCredential loginCredential = new LoginCredential();
            loginCredential.setAccount("test_user");
            loginCredential.setPassword(passwordEncoder.encode("test"));
            endUser.setCredential(loginCredential);

            UserProfile endUserProfile = new UserProfile();
            endUserProfile.setFirstName("firstName");
            endUserProfile.setLastName("lastName");
            endUserProfile.setMiddleName("middleName");
            Address address = new Address();
            address.setZipcode("123-456");
            address.setMainAddress("main address");
            address.setDetailAddress("detail address");
            endUserProfile.setGender(Gender.MALE);
            endUserProfile.setBirthDate("2017-01-01");
            endUserProfile.setCountry("Korea");
            endUserProfile.setLanguage("Korean");
            endUserProfile.setMobilePhoneNo("010-1111-"+ new Random()
                    .ints(4, 0, 9)
                    .mapToObj(i -> String.valueOf(i))
                    .collect(Collectors.joining()));

            endUserProfile.setNickName("nickName");
            endUserProfile.setTimezone("GMT+9");
            endUserProfile.setAddress(address);
            endUser.setProfile(endUserProfile);

            return endUserRepository.save(endUser);
        }catch (Throwable t) {
            System.err.println(t.getMessage());
            throw new RuntimeException(t);
        }
    }

     public static void prepareApplicatonEndUsersForDev(Application createdApp, EndUser endUser, ApplicationRepository applicationRepository) {
        ApplicationEndUser applicationEndUser = new ApplicationEndUser();
        applicationEndUser.setApplication(createdApp);
        applicationEndUser.setEndUser(endUser);
        createdApp.getApplicationEndUsers().add(applicationEndUser);
        endUser.getApplicationEndUsers().add(applicationEndUser);
//        applicationRepository.save(createdApp);
    }

    public static EndUser createTestEndUser(String userName) {
        return EndUserTestUtil.createTestEndUser(userName);
    }

    public static EndUser createTestEndUser(String userName, String email) {
        EndUser endUser = createTestEndUser(userName);
        endUser.setEmail(email);
        return endUser;
    }

    public static EndUser createTestEndUser(String userName, String email, String mobileNo) {
        EndUser endUser = createTestEndUser(userName, email);
        endUser.getProfile().setMobilePhoneNo(mobileNo);
        return endUser;
    }
}
