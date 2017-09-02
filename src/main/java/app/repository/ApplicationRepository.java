package app.repository;

import app.domain.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ApplicationRepository extends JpaRepository<Application, String> {
    //    @Secured("hasRole('AP_MANAGER')")
    Page<Application> findByManagerId(String managerId, Pageable pageable);

    Page<Application> findByOwnerId(String ownerId, Pageable pageable);

    //    @Secured("hasRole('AP_MANAGER')")
    Application findByIdAndManagerId(String id, String managerId);

    //    @Secured("hasRole('AP_MANAGER')")
    Page<Application> findByNameAndManagerId(String appName, String managerId, Pageable pageable);

    Application findByIdAndOwnerId(String applicationId, String ownerId);

    // #DEPPJT-286
    Page<Application> findByNameAndOwnerId(String appName, String ownerId, Pageable pageable);

    @Query("select app from TENANT_APPLICATION app where owner.loginCredential.account = :ownerAccount and app.name like %:appName%")
    Page<Application> findByNameLikeAndOwnerAccount(@Param("appName") String appName, @Param("ownerAccount") String ownerAccount, Pageable pageable);

    @Query("select app from TENANT_APPLICATION app where owner.loginCredential.account = :ownerAccount and app.status = :status")
    Page<Application> findByStatusAndOwnerAccount(
            @Param("status") Application.ApplicationStatus status,
            @Param("ownerAccount") String ownerAccount,
            Pageable pageable);

    @Query("select app from TENANT_APPLICATION app where owner.loginCredential.account = :ownerAccount and app.isDisabledNewUser = :isDisabledNewUser")
    Page<Application> findByIsDisabledNewUserAndOwnerAccount(
            @Param("isDisabledNewUser") boolean isDisabledNewUser,
            @Param("ownerAccount") String ownerAccount,
            Pageable pageable);

    @Query("select app from TENANT_APPLICATION app where app.name like %:appName%")
    Page<Application> findByNameLike(@Param("appName") String appName, Pageable pageable);

    Page<Application> findByStatus(Application.ApplicationStatus status, Pageable pageable);

    Page<Application> findByIsDisabledNewUser(boolean isDisabledNewUser, Pageable pageable);
}
