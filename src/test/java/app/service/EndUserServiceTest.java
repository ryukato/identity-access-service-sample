package app.service;

import app.domain.EndUser;
import app.domain.EndUserStatus;
import app.error.*;
import app.repository.EndUserRepository;
import app.util.EndUserTestUtil;
import org.hamcrest.core.IsNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.internal.matchers.Equality;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class EndUserServiceTest {

    @Mock
    private EndUserRepository endUserRepository;

    @Mock
    private ApplicationService applicationService;

    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

    private EndUserService endUserService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Before
    public void setUp() {
        this.endUserService = new EndUserService(endUserRepository, applicationService, passwordEncoder);
    }

    @After
    public void tearDown() {}

    @Test
    public void createEndUser() {
        EndUser endUser = EndUserTestUtil.createTestEndUser("test_user");
        String test_application = "test_application";
        when(endUserRepository.save(endUser)).thenReturn(endUser);
        when(applicationService.addApplicationUser(test_application, endUser)).thenReturn(true);

        EndUser createdEndUser = endUserService.createEndUser(test_application, endUser);
        verify(endUserRepository, times(1)).save(endUser);
        verify(applicationService, times(1)).addApplicationUser(test_application, endUser);
        assertThat("Created EndUser is not null", createdEndUser, IsNull.notNullValue());
        assertThat("EndUser and Created EndUser are equal", Equality.areEqual(endUser, createdEndUser));
    }

    @Test
    public void createEndUser_but_already_exsiting() {
        EndUser endUser = EndUserTestUtil.createTestEndUser("already_existing");
        String test_application = "test_application";
        when(endUserRepository.findByApplicationAndCredentialAccount(test_application, endUser.getCredential().getAccount())).thenReturn(endUser);

        try {
            endUserService.createEndUser(test_application, endUser);
            fail(String.format("%s already existing", endUser.getCredential().getAccount()));
        } catch (SameUserNameFoundException e){}
    }

    @Test
    public void createEndUser_but_duplicated_email_endUser() {
        EndUser createEndUser = EndUserTestUtil.createTestEndUser("same_email_user");
        EndUser endUser = EndUserTestUtil.createTestEndUser("same_email_user");
        endUser.setId(UUID.randomUUID().toString());
        endUser.setEmail(createEndUser.getEmail());
        String test_application = "test_application";
        when(endUserRepository.findByApplicationAndEmail(test_application, endUser.getEmail())).thenReturn(endUser);

        try {
            endUserService.createEndUser(test_application, createEndUser);
            fail(String.format("Same email is not allowed", endUser.getCredential().getAccount()));
        } catch (DuplicatedEmailUserFoundException e){}
    }

    @Test
    public void createEndUser_but_duplicated_mobile_endUser() {
        EndUser createEndUser = EndUserTestUtil.createTestEndUser("same_mobile_user");
        EndUser endUser = EndUserTestUtil.createTestEndUser("same_mobile_user");
        endUser.setId(UUID.randomUUID().toString());
        endUser.getProfile().setMobilePhoneNo(createEndUser.getProfile().getMobilePhoneNo());
        String test_application = "test_application";
        when(endUserRepository.findByApplicationAndMobileNo(test_application, endUser.getProfile().getMobilePhoneNo())).thenReturn(endUser);

        try {
            endUserService.createEndUser(test_application, createEndUser);
            fail(String.format("Same mobile no is not allowed", endUser.getCredential().getAccount()));
        } catch (DuplicatedMobileNoFoundException e){}
    }

    @Test
    public void createEndUser_with_out_login_credential() {
        EndUser endUser = EndUserTestUtil.createTestEndUser("test_user");
        endUser.setCredential(null);
        String test_application = "test_application";

        try {
            EndUser createdEndUser = endUserService.createEndUser(test_application, endUser);
            fail();
        }catch (NoUserLoginCredentialException e) {}

    }

    @Test
    public void createEndUser_with_out_login_credential_account() {
        EndUser endUser = EndUserTestUtil.createTestEndUser("test_user");
        endUser.getCredential().setAccount(null);
        String test_application = "test_application";

        try {
            EndUser createdEndUser = endUserService.createEndUser(test_application, endUser);
            fail();
        }catch (NoUserLoginCredentialException e) {}

    }

    @Test
    public void updateEndUser() {
        EndUser endUser = EndUserTestUtil.createTestEndUser("test_user");
        String existingEndUserId = "test_user_id";
        when(endUserRepository.findById(existingEndUserId)).thenReturn(endUser);
        when(endUserRepository.save(endUser)).thenReturn(endUser);

        EndUser updatedEndUser = endUserService.updateEndUser(existingEndUserId, endUser);
        verify(endUserRepository, times(1)).save(endUser);
        assertThat("Updated EndUser is not null", updatedEndUser, IsNull.notNullValue());
        assertThat("EndUser and Updated EndUser are equal", Equality.areEqual(endUser, updatedEndUser));
    }

    @Test
    public void updateEndUser_only_email_address() {
        EndUser updateEndUser = EndUserTestUtil.createTestEndUser("test_user");
        updateEndUser.setEmail("new_end_user_email@test_new.com");
        updateEndUser.setId(UUID.randomUUID().toString());
        EndUser endUser = EndUserTestUtil.createTestEndUser("test_user");
        endUser.setId(updateEndUser.getId());
        String existingEndUserId = updateEndUser.getId();
        when(endUserRepository.findById(existingEndUserId)).thenReturn(endUser);
        when(endUserRepository.save(updateEndUser)).thenReturn(updateEndUser);

        EndUser updatedEndUser = endUserService.updateEndUser(existingEndUserId, updateEndUser);
        verify(endUserRepository, times(1)).save(updateEndUser);
        assertThat("Updated EndUser is not null", updatedEndUser, IsNull.notNullValue());
        assertThat("EndUser and Updated EndUser are equal", Equality.areEqual(endUser, updatedEndUser));
    }

    @Test
    public void updateEndUser_when_not_exists_endUser() {
        EndUser endUser = EndUserTestUtil.createTestEndUser("not_exist_user");
        String existingEndUserId = "not_exist_user";
        when(endUserRepository.findById(existingEndUserId)).thenReturn(null);

        try {
            endUserService.updateEndUser(existingEndUserId, endUser);
            fail();
        } catch (RecordNotFoundException e) {}
    }

    @Test
    public void updateEndUserProfile() {
        EndUser endUser = EndUserTestUtil.createTestEndUser("test_user");
        String existingEndUserId = "test_user_id";
        when(endUserRepository.findById(existingEndUserId)).thenReturn(endUser);
        when(endUserRepository.save(endUser)).thenReturn(endUser);

        EndUser updatedEndUser = endUserService.updateEndUserProfile(existingEndUserId, endUser);
        verify(endUserRepository, times(1)).save(endUser);
        assertThat("Updated EndUser is not null", updatedEndUser, IsNull.notNullValue());
        assertThat("EndUser and Updated EndUser are equal", Equality.areEqual(endUser, updatedEndUser));
    }

    @Test
    public void updateEndUserProfile_when_not_exists_endUser() {
        EndUser endUser = EndUserTestUtil.createTestEndUser("not_exist_user");
        String existingEndUserId = "not_exist_user";
        when(endUserRepository.findById(existingEndUserId)).thenReturn(null);

        try {
            endUserService.updateEndUserProfile(existingEndUserId, endUser);
            fail();
        } catch (RecordNotFoundException e) {}
    }

    @Test
    public void deleteEndUser() {
        EndUser endUser = EndUserTestUtil.createTestEndUser("test_user");
        String existingEndUserId = "test_user_id";
        String test_application = "test_application";
        when(endUserRepository.findById(existingEndUserId)).thenReturn(endUser);

        endUserService.deleteEndUser(test_application, existingEndUserId);
        verify(endUserRepository, times(1)).delete(existingEndUserId);
    }

    @Test
    public void deleteEndUser_not_existing_endUser() {
        EndUser endUser = EndUserTestUtil.createTestEndUser("test_user");
        String existingEndUserId = "not_exists_end_user_id";
        String test_application = "test_application";
        when(endUserRepository.findById(existingEndUserId)).thenReturn(null);

        try {
            endUserService.deleteEndUser(test_application, existingEndUserId);
            fail();
        } catch (RecordNotFoundException e){}
    }

    @Test
    public void unregisterEndUser() {
        String test_application = "test_application";
        EndUser endUser = EndUserTestUtil.createTestEndUser("test_user");
        String existingEndUserId = "test_user_id";
        when(endUserRepository.findById(existingEndUserId)).thenReturn(endUser);

        EndUser unregisteredEndUser = endUserService.unregisterEndUser(test_application, existingEndUserId);
        assertThat("Un-registered EndUser Status is TERMINATED ", Equality.areEqual(unregisteredEndUser.getStatus(), EndUserStatus.TERMINATED));
    }

    @Test
    public void unregisterEndUser_not_existing_endUser() {
        String test_application = "test_application";
        String existingEndUserId = "not_exists_end_user_id";
        when(endUserRepository.findById(existingEndUserId)).thenReturn(null);

        try {
            EndUser unregisteredEndUser = endUserService.unregisterEndUser(test_application, existingEndUserId);
            fail();
        } catch (RecordNotFoundException e){}
    }

    @Test
    public void unregisterEndUser_already_unregistered_endUser() {
        String test_application = "test_application";
        EndUser endUser = EndUserTestUtil.createTestEndUser("test_user");
        endUser.setStatus(EndUserStatus.TERMINATED);
        String existingEndUserId = "test_user_id";
        when(endUserRepository.findById(existingEndUserId)).thenReturn(endUser);

        EndUser unregisteredEndUser = endUserService.unregisterEndUser(test_application, existingEndUserId);
        assertThat("Un-registered EndUser Status is TERMINATED ", Equality.areEqual(unregisteredEndUser.getStatus(), EndUserStatus.TERMINATED));
    }

    @Test
    public void registerEndUser() {
        String test_application = "test_application";
        EndUser endUser = EndUserTestUtil.createTestEndUser("test_user");

    }

    private EndUser createTestEndUser(String userName) {
        return EndUserTestUtil.createTestEndUser(userName);
    }

    private void setEndUserProfile(EndUser endUser) {
        EndUserTestUtil.setEndUserProfile(endUser);
    }

    private void setLoginCredential(String userName, EndUser endUser) {
        EndUserTestUtil.setLoginCredential(userName, endUser);
    }

    private Principal principalForTest(final String name) {
        return EndUserTestUtil.principalForTest(name);
    }
}
