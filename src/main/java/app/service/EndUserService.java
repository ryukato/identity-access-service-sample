package app.service;

import app.domain.EndUser;
import app.domain.EndUserStatus;
import app.domain.PasswordUpdateRequest;
import app.error.FailToAddUserToApplicationException;
import app.error.RecordNotFoundException;
import app.repository.EndUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/*
Service 객체에는 웹 관련 객체(e.g. URI, ResponseEntity...)에 대한 의존성은 최대한 없애야 한다고 생각.
 */
@Service
@Transactional
public class EndUserService {
    private final Logger log = LoggerFactory.getLogger(EndUserService.class);
    private final EndUserRepository endUserRepository;
    private final ApplicationService applicationService;
    private final EndUserRegistrationValidator endUserRegistrationValidator;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public EndUserService(
            EndUserRepository endUserRepository,
            ApplicationService applicationService,
            PasswordEncoder passwordEncoder
    ) {
        this.endUserRepository = endUserRepository;
        this.applicationService = applicationService;
        this.endUserRegistrationValidator = new EndUserRegistrationValidator(endUserRepository, applicationService);
        this.passwordEncoder = passwordEncoder;
    }

    public EndUser createEndUser(String applicationId, EndUser endUser) {
        endUserRegistrationValidator.validateEndUserRegistrationConstraints(applicationId, endUser);
        endUser.getCredential().setPassword(passwordEncoder.encode(endUser.getCredential().getPassword()));
        endUser.setStatus(EndUserStatus.CREATED);
        EndUser createdUserEndUser = saveEndUser(endUser);
        // TODO : notify EndUser created Event
        log.info("End-user {} is created." , endUser.getCredential().getAccount());
        boolean result = applicationService.addApplicationUser(applicationId, createdUserEndUser);
        if (!result) {
            throw new FailToAddUserToApplicationException(String.format("Fail to add user(%s) to application(%s)", endUser.getId(), applicationId));
        }
        return createdUserEndUser;
    }

    private EndUser saveEndUser(EndUser endUser) {
        return endUserRepository.save(endUser);
    }

    public EndUser updateEndUser(
            String id,
            EndUser endUser) {

        EndUser exitingEndUser = findExistingEndUser(id);

        updateEndUserEmail(endUser, exitingEndUser);
        updateEndUserStatus(endUser, exitingEndUser);
        updateEndUserProfile(endUser, exitingEndUser);
        // TODO : notify EndUser updated Event
        log.info("End-user {} is updated." , endUser.getId());

        return saveEndUser(exitingEndUser);
    }

    private void updateEndUserProfile(EndUser endUser, EndUser exitingEndUser) {
        if (endUser.getProfile() != null && !endUser.getProfile().equals(exitingEndUser.getProfile())) {
            exitingEndUser.setProfile(endUser.getProfile());
        }
    }

    private void updateEndUserStatus(EndUser endUser, EndUser exitingEndUser) {
        EndUserStatus endUserStatus = endUser.getStatus();
        if (endUserStatus != null && !endUserStatus.equals(exitingEndUser.getStatus())) {
            exitingEndUser.setStatus(endUserStatus);
        }
    }

    private void updateEndUserEmail(EndUser endUser, EndUser exitingEndUser) {
        if (endUser.getEmail() != null &&
                !endUser.getEmail().isEmpty() &&
                !endUser.getEmail().equals(exitingEndUser.getEmail())) {
            exitingEndUser.setEmail(endUser.getEmail());
        }
    }

    public EndUser updateEndUserProfile(
            String id,
            EndUser endUser) {

        EndUser exitingEndUser = findExistingEndUser(id);
        updateEndUserProfile(endUser, exitingEndUser);
        // TODO : notify EndUser Profile updated Event
        log.info("End-user {} Profile is updated." , endUser.getId(), endUser.getProfile());
        return saveEndUser(exitingEndUser);
    }

    public boolean deleteEndUser(String applicationId, String id) {
        EndUser endUser = findExistingEndUser(id);
        endUserRepository.delete(id);
        // TODO : notify EndUser deleted Event
        log.info("End-user {} is deleted." , endUser.getCredential().getAccount());
        return true;
    }

    public EndUser unregisterEndUser(
            String applicationId,
            String id) {
        // TODO : notify EndUser un-registered Event
        EndUser unregisteredEndUser = terminate(applicationId, id);
        log.info("End-user {} un-registration is completed." , id);
        return unregisteredEndUser;
    }

    public EndUser register(String applicationId, EndUser endUser) {
        endUserRegistrationValidator.validateEndUserRegistrationConstraints(applicationId, endUser);
        endUser.getCredential().setPassword(passwordEncoder.encode(endUser.getCredential().getPassword()));
        endUser.setStatus(EndUserStatus.CREATED);
        EndUser registeredEndUser = saveEndUser(endUser);
        boolean result = applicationService.addApplicationUser(applicationId, registeredEndUser);
        if (!result) {
            throw new FailToAddUserToApplicationException(String.format("Fail to add user(%s) to application(%s)", endUser.getId(), applicationId));
        }
        // TODO : notify EndUser registered Event
        log.info("End-user {} registration is completed." , endUser.getCredential().getAccount());
        return registeredEndUser;
    }

    public EndUser activate(String applicationId, String id) {
        EndUser endUser = findExistingEndUser(id);
        Optional.of(endUser)
                .filter(e -> e.getStatus() != EndUserStatus.ACTIVE)
                .ifPresent(
                        e -> {
                            e.setStatus(EndUserStatus.ACTIVE); EndUser activatedEndUser = saveEndUser(endUser);
                        }
                );
        // TODO : notify EndUser Status changed Event
        log.info("End-user {} activation is completed." , endUser.getCredential().getAccount());
        return endUser;
    }

    public EndUser terminate(String applicationId, String id) {
        EndUser endUser = findExistingEndUser(id);
        Optional.of(endUser)
                .filter(e -> e.getStatus() != EndUserStatus.TERMINATED)
                .ifPresent(e -> {
                    e.setStatus(EndUserStatus.TERMINATED);
                    EndUser activatedEndUser = saveEndUser(endUser);
                });
        // TODO : notify EndUser Status changed Event
        log.info("End-user {} termination is completed." , endUser.getCredential().getAccount());
        return endUser;
    }

    public EndUser suspend(String applicationId, String id) {
        EndUser endUser = findExistingEndUser(id);
        Optional.of(endUser)
                .filter(e -> e.getStatus() != EndUserStatus.SUSPENDED)
                .ifPresent(e -> {
                    e.setStatus(EndUserStatus.SUSPENDED);
                    EndUser activatedEndUser = saveEndUser(endUser);
                });
        // TODO : notify EndUser Status changed Event
        log.info("End-user {} suspending is completed." , endUser.getCredential().getAccount());
        return endUser;
    }

    private EndUser findExistingEndUser(String id) {
        EndUser endUser = endUserRepository.findById(id);
        Optional.ofNullable(endUser).<RecordNotFoundException>orElseThrow(RecordNotFoundException::new);
        return endUser;
    }


    public EndUser updatePassword(String id, PasswordUpdateRequest passwordUpdateRequest) {

        EndUser endUser = findExistingEndUser(id);
        String encodedPassword = endUser.getCredential().getPassword();
        if (passwordEncoder.matches(passwordUpdateRequest.getCurrentPassword(), encodedPassword)) {
            endUser.getCredential().setPassword(passwordEncoder.encode(passwordUpdateRequest.getNewPassword()));
            return endUserRepository.save(endUser);
        }
        return endUser;
    }
}
