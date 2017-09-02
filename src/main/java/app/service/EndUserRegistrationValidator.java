package app.service;

import app.domain.EndUser;
import app.domain.LoginCredential;
import app.error.DuplicatedEmailUserFoundException;
import app.error.DuplicatedMobileNoFoundException;
import app.error.NoUserLoginCredentialException;
import app.error.SameUserNameFoundException;
import app.repository.EndUserRepository;

import java.util.Optional;

class EndUserRegistrationValidator {

    private final EndUserRepository endUserRepository;
    private final ApplicationService applicationService;

    EndUserRegistrationValidator(EndUserRepository endUserRepository, ApplicationService applicationService) {
        this.endUserRepository = endUserRepository;
        this.applicationService = applicationService;
    }

    void validateEndUserRegistrationConstraints(String applicationId, EndUser endUser) {
        validateApplicationExists(applicationId);
        validateHaveLoginCredential(endUser);
        validateNoDuplicatedUserName(applicationId, endUser);
        validateNoDuplicatedUserEmail(applicationId, endUser);
        validateNoDuplicatedUserMobile(applicationId, endUser);
    }

    private void validateApplicationExists(String applicationId) {
        applicationService.checkApplicationExists(applicationId);
    }

    private void validateNoDuplicatedUserMobile(String applicationId, EndUser endUser) {
        if (endUser.getProfile() == null ||
                endUser.getProfile().getMobilePhoneNo() == null ||
                endUser.getProfile().getMobilePhoneNo().isEmpty()) {
            return;
        }

        Optional.ofNullable(endUserRepository.findByApplicationAndMobileNo(applicationId, endUser.getProfile().getMobilePhoneNo()))
                .filter(e -> !e.getId().equals(endUser.getId())) // Fix DEPPJT-298
                .ifPresent(e -> {
                    throw new DuplicatedMobileNoFoundException(String.format("Same mobile no(%s) is not allowed", endUser.getEmail()));
                }
        );
    }

    private void validateNoDuplicatedUserEmail(String applicationId, EndUser endUser) {
        if (endUser.getEmail() == null ||
                endUser.getEmail().isEmpty()) {
            return;
        }
        Optional.ofNullable(endUserRepository.findByApplicationAndEmail(applicationId, endUser.getEmail()))
                .filter(e -> !e.getId().equals(endUser.getId())) // Fix DEPPJT-298
                .ifPresent(e -> {
                    throw new DuplicatedEmailUserFoundException(String.format("Same email(%s) is not allowed", endUser.getEmail()));
                });
    }

    private void validateNoDuplicatedUserName(String applicationId, EndUser endUser) {

        Optional.ofNullable(endUserRepository.findByApplicationAndCredentialAccount(applicationId, endUser.getCredential().getAccount()))
                .ifPresent(e -> {
                    throw new SameUserNameFoundException(String.format("%s already existing", endUser.getCredential().getAccount()));
                }
        );
    }

    private void validateHaveLoginCredential(EndUser endUser) {
        LoginCredential loginCredential = endUser.getCredential();
        Optional.ofNullable(loginCredential).<NoUserLoginCredentialException>orElseThrow(NoUserLoginCredentialException::new);
        Optional<String> account = Optional.ofNullable(loginCredential.getAccount());
        account.<NoUserLoginCredentialException>orElseThrow(NoUserLoginCredentialException::new);
        Optional<String> password = Optional.ofNullable(loginCredential.getPassword());
        password.<NoUserLoginCredentialException>orElseThrow(NoUserLoginCredentialException::new);

        account.filter(a -> a.isEmpty()).ifPresent(a -> new NoUserLoginCredentialException());
        password.filter(a -> a.isEmpty()).ifPresent(a -> new NoUserLoginCredentialException());
    }
}