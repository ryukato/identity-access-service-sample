package app.repository;

import app.IdentityAccessServiceApplication;
import app.domain.*;
import app.util.TenantTestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = IdentityAccessServiceApplication.class)
@Transactional
public class EndUserRepositoryTest {
    private static final PageRequest PAGE_REQUEST = new PageRequest(0, 10);
    @Autowired
    private EndUserRepository endUserRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Application application;
    private EndUser endUser;
    private Tenant tenant;
    @Before
    public void setUp() {
        application = new Application();
        application.setManagerId("test_manager");
        application.setName("test_application");
        application.setStatus(Application.ApplicationStatus.ACTIVE);
        application.setDisabledNewUser(false);
        application.setApiKey("test_api_key");
        applicationRepository.save(application);

        Application application2 = new Application();
        application2.setManagerId("test_manager");
        application2.setName("test_application2");
        application2.setStatus(Application.ApplicationStatus.ACTIVE);
        application2.setDisabledNewUser(false);
        application2.setApiKey("test_api_key");
        applicationRepository.save(application2);


        endUser = new EndUser();
        endUser.setStatus(EndUserStatus.CREATED);
        endUser.setEmail("test_user1@test.com");
        LoginCredential loginCredential = new LoginCredential();
        loginCredential.setAccount("test_user1");
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
        endUserProfile.setMobilePhoneNo("010-9999-5555");
        endUserProfile.setNickName("nickName");
        endUserProfile.setTimezone("GMT+9");
        endUserProfile.setAddress(address);
        endUser.setProfile(endUserProfile);

        endUserRepository.save(endUser);

        ApplicationEndUser applicationEndUser = new ApplicationEndUser();
        applicationEndUser.setApplication(application);
        applicationEndUser.setEndUser(endUser);
        endUser.getApplicationEndUsers().add(applicationEndUser);
        application.getApplicationEndUsers().add(applicationEndUser);

//        applicationRepository.save(application);

        tenant = TenantTestUtil.createTestTenant();
        tenant.getApplications().add(application);
        application.setOwner(tenant);
        tenantRepository.save(tenant);
        applicationRepository.save(application);

        tenant.getApplications().add(application2);
        application2.setOwner(tenant);
        tenantRepository.save(tenant);
        applicationRepository.save(application2);
    }

    @Test
    public void findByApplication() {
        Page<EndUser> endUsers = endUserRepository.findByApplication(application.getId(), new PageRequest(0, 10));
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findById() {
        EndUser foundEndUser = endUserRepository.findById(endUser.getId());
        assertNotNull(foundEndUser);
        assertEquals(foundEndUser.getId(), endUser.getId());
    }

    @Test
    public void findByEmail() {
        EndUser foundEndUser = endUserRepository.findByEmail(
                endUser.getEmail(),
                application.getId(),
                tenant.getId()
        );
        assertNotNull(foundEndUser);
        assertEquals(foundEndUser.getEmail(), endUser.getEmail());
    }

    @Test
    public void findByEmail_without_applicationId() {
        EndUser foundEndUser = endUserRepository.findByEmail(
                endUser.getEmail(),
                tenant.getId());
        assertNotNull(foundEndUser);
        assertEquals(foundEndUser.getEmail(), endUser.getEmail());
    }

    @Test
    public void findByEmailLike() {
        Page<EndUser> endUsers = endUserRepository.findByEmailLike(
                endUser.getEmail(),
                application.getId(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByEmailLike_without_applicationId() {
        Page<EndUser> endUsers = endUserRepository.findByEmailLike(
                endUser.getEmail(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByEmailLike_with_only_start_letter() {
        Page<EndUser> endUsers = endUserRepository.findByEmailLike("t", tenant.getId(), PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileFirstName() {
        Page<EndUser> endUsers = endUserRepository.findByProfileFirstName(
                endUser.getProfile().getFirstName(),
                application.getId(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileFirstName_without_applicationId() {
        Page<EndUser> endUsers = endUserRepository.findByProfileFirstName(
                endUser.getProfile().getFirstName(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileFirstNameLike() {
        Page<EndUser> endUsers = endUserRepository.findByProfileFirstNameLike(
                endUser.getProfile().getFirstName(),
                application.getId(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileFirstNameLike_without_applicationId() {
        Page<EndUser> endUsers = endUserRepository.findByProfileFirstNameLike(
                endUser.getProfile().getFirstName(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileFirstNameLike_with_only_start_letter() {
        Page<EndUser> endUsers = endUserRepository.findByProfileFirstNameLike(
                "f",
                application.getId(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileLastName() {
        Page<EndUser> endUsers = endUserRepository.findByProfileLastName(
                endUser.getProfile().getLastName(),
                application.getId(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileLastNameLike() {
        Page<EndUser> endUsers = endUserRepository.findByProfileLastNameLike(
                endUser.getProfile().getLastName(),
                application.getId(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileLastName_without_applicationId() {
        Page<EndUser> endUsers = endUserRepository.findByProfileLastName(
                endUser.getProfile().getLastName(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileLastNameLike_without_applicationId() {
        Page<EndUser> endUsers = endUserRepository.findByProfileLastNameLike(
                endUser.getProfile().getLastName(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileLastNameLike_with_only_start_letter() {
        Page<EndUser> endUsers = endUserRepository.findByProfileLastNameLike(
                "l",
                application.getId(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }


    @Test
    public void findByProfileMiddleName() {
        Page<EndUser> endUsers = endUserRepository.findByProfileMiddleName(
                endUser.getProfile().getMiddleName(),
                application.getId(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileMiddleNameLike() {
        Page<EndUser> endUsers = endUserRepository.findByProfileMiddleNameLike(
                endUser.getProfile().getMiddleName(),
                application.getId(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileMiddleName_without_applicationId() {
        Page<EndUser> endUsers = endUserRepository.findByProfileMiddleName(
                endUser.getProfile().getMiddleName(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileMiddleNameLike_without_applicationId() {
        Page<EndUser> endUsers = endUserRepository.findByProfileMiddleNameLike(
                endUser.getProfile().getMiddleName(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileMiddleNameLike_with_only_start_letter() {
        Page<EndUser> endUsers = endUserRepository.findByProfileMiddleNameLike(
                "m",
                application.getId(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileNickName() {
        Page<EndUser> endUsers = endUserRepository.findByProfileNickName(
                endUser.getProfile().getNickName(),
                application.getId(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileNickNameLike() {
        Page<EndUser> endUsers = endUserRepository.findByProfileNickNameLike(
                endUser.getProfile().getNickName(),
                application.getId(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileNickName_without_applicationId() {
        Page<EndUser> endUsers = endUserRepository.findByProfileNickName(
                endUser.getProfile().getNickName(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileNickNameLike_without_applicationId() {
        Page<EndUser> endUsers = endUserRepository.findByProfileNickNameLike(
                endUser.getProfile().getNickName(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileNickNameLike_with_only_start_letter() {
        Page<EndUser> endUsers = endUserRepository.findByProfileNickNameLike(
                "n",
                application.getId(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileMobilePhoneNo() {
        EndUser foundEndUser = endUserRepository.findByProfileMobilePhoneNo(
                endUser.getProfile().getMobilePhoneNo(),
                application.getId(),
                tenant.getId()
                );
        assertNotNull(foundEndUser);
        assertEquals(foundEndUser.getProfile().getMobilePhoneNo(), endUser.getProfile().getMobilePhoneNo());
    }

    @Test
    public void findByProfileGender() {
        Page<EndUser> endUsers = endUserRepository.findByProfileGender(
                Gender.MALE,
                application.getId(),
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findByProfileMobilePhoneNo_without_applicationId() {
        EndUser foundEndUser = endUserRepository.findByProfileMobilePhoneNo(
                endUser.getProfile().getMobilePhoneNo(),
                tenant.getId()
        );
        assertNotNull(foundEndUser);
        assertEquals(foundEndUser.getProfile().getMobilePhoneNo(), endUser.getProfile().getMobilePhoneNo());
    }

    @Test
    public void findByProfileGender_without_applicationId() {
        Page<EndUser> endUsers = endUserRepository.findByProfileGender(
                Gender.MALE,
                tenant.getId(),
                PAGE_REQUEST);
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findApplicationEndUsers() {
        Page<EndUser> endUsers = endUserRepository.findApplicationEndUsers(application.getId(), new PageRequest(0, 10));
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findAllEndTenantIdUsersForTenant() {
        String tenantId = tenant.getId();
        Page<EndUser> endUsers = endUserRepository.findAllEndUsersForTenant(tenantId, new PageRequest(0, 10));
        assertNotNull(endUsers);
        assertTrue(endUsers.hasContent());
    }

    @Test
    public void findUserCountTenant() {
        String tenantId = tenant.getId();
//       Page<EndUser> endUsers = endUserRepository.findAllEndUsersForTenant(tenantId, new PageRequest(0, 10));
        List<Object[]> endUsers = endUserRepository.findCountsEndUserByStatusForTenant(tenantId);
        Object[] endUser = endUsers.get(0);
        assertEquals(endUser[0], EndUserStatus.CREATED );
        assertEquals(endUser[1], 1L );
    }

}
