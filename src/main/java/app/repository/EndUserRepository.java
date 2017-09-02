package app.repository;

import app.domain.EndUser;
import app.domain.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EndUserRepository extends JpaRepository<EndUser, String> {

    @Secured("hasRole('SYS_ADMIN')")
    Page<EndUser> findAll(Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.owner.id = :tenantId ")
    Page<EndUser> findAllOfTenant(
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where (a.pk.application.id = :applicationId)")
    Page<EndUser> findApplicationEndUsers(
            @Param("applicationId") String applicationId,
            Pageable pageable);

    EndUser findById(String id);

    EndUser findByCredentialAccount(String account);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where (a.pk.application.id = :applicationId) " +
            "and user.credential.account = :account")
    EndUser findByApplicationAndCredentialAccount(
            @Param("applicationId") String applicationId,
            @Param("account") String account);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where (a.pk.application.id = :applicationId)")
    Page<EndUser> findByApplication(@Param("applicationId") String applicationId, Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
        "where a.pk.application.id = :applicationId " +
        "and a.pk.application.owner.id = :tenantId " +
        "and user.email = :email")
    EndUser findByEmail(
            @Param("email") String email,
            @Param("applicationId") String applicationId,
            @Param("tenantId") String tenantId
    );

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.owner.id = :tenantId " +
            "and user.email = :email")
    EndUser findByEmail(
            @Param("email") String email,
            @Param("tenantId") String tenantId
    );

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.id = :applicationId " +
            "and a.pk.application.owner.id = :tenantId " +
            "and user.email like %:email%")
    Page<EndUser> findByEmailLike(
            @Param("email") String email,
            @Param("applicationId") String applicationId,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.owner.id = :tenantId " +
            "and user.email like %:email%")
    Page<EndUser> findByEmailLike(
            @Param("email") String email,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where (a.pk.application.id = :applicationId) " +
            "and user.email = :email")
    EndUser findByApplicationAndEmail(
            @Param("applicationId") String applicationId,
            @Param("email") String email);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.id = :applicationId " +
            "and a.pk.application.owner.id = :tenantId " +
            "and user.profile.firstName = :firstName")
    Page<EndUser> findByProfileFirstName(
            @Param("firstName") String firstName,
            @Param("applicationId") String applicationId,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.owner.id = :tenantId " +
            "and user.profile.firstName = :firstName")
    Page<EndUser> findByProfileFirstName(
            @Param("firstName") String firstName,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.id = :applicationId " +
            "and a.pk.application.owner.id = :tenantId " +
            "and user.profile.firstName like %:firstName%")
    Page<EndUser> findByProfileFirstNameLike(
            @Param("firstName") String firstName,
            @Param("applicationId") String applicationId,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.owner.id = :tenantId " +
            "and user.profile.firstName like %:firstName%")
    Page<EndUser> findByProfileFirstNameLike(
            @Param("firstName") String firstName,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.id = :applicationId " +
            "and a.pk.application.owner.id = :tenantId " +
            "and user.profile.lastName like %:lastName%")
    Page<EndUser> findByProfileLastNameLike(
            @Param("lastName") String lastName,
            @Param("applicationId") String applicationId,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.owner.id = :tenantId " +
            "and user.profile.lastName like %:lastName%")
    Page<EndUser> findByProfileLastNameLike(
            @Param("lastName") String lastName,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.id = :applicationId " +
            "and a.pk.application.owner.id = :tenantId " +
            "and user.profile.lastName = :lastName")
    Page<EndUser> findByProfileLastName(
            @Param("lastName") String lastName,
            @Param("applicationId") String applicationId,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.owner.id = :tenantId " +
            "and user.profile.lastName = :lastName")
    Page<EndUser> findByProfileLastName(
            @Param("lastName") String lastName,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.id = :applicationId " +
            "and a.pk.application.owner.id = :tenantId " +
            "and user.profile.middleName = :middleName")
    Page<EndUser> findByProfileMiddleName(
            @Param("middleName") String middleName,
            @Param("applicationId") String applicationId,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.owner.id = :tenantId " +
            "and user.profile.middleName = :middleName")
    Page<EndUser> findByProfileMiddleName(
            @Param("middleName") String middleName,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.id = :applicationId " +
            "and a.pk.application.owner.id = :tenantId " +
            "and user.profile.middleName like %:middleName%")
    Page<EndUser> findByProfileMiddleNameLike(
            @Param("middleName") String middleName,
            @Param("applicationId") String applicationId,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.owner.id = :tenantId " +
            "and user.profile.middleName like %:middleName%")
    Page<EndUser> findByProfileMiddleNameLike(
            @Param("middleName") String middleName,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.id = :applicationId " +
            "and a.pk.application.owner.id = :tenantId " +
            "and user.profile.nickName = :nickName")
    Page<EndUser> findByProfileNickName(
            @Param("nickName") String nickName,
            @Param("applicationId") String applicationId,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.owner.id = :tenantId " +
            "and user.profile.nickName = :nickName")
    Page<EndUser> findByProfileNickName(
            @Param("nickName") String nickName,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.id = :applicationId " +
            "and a.pk.application.owner.id = :tenantId " +
            "and user.profile.nickName like %:nickName%")
    Page<EndUser> findByProfileNickNameLike(
            @Param("nickName") String nickName,
            @Param("applicationId") String applicationId,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.owner.id = :tenantId " +
            "and user.profile.nickName like %:nickName%")
    Page<EndUser> findByProfileNickNameLike(
            @Param("nickName") String nickName,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.id = :applicationId " +
            "and a.pk.application.owner.id = :tenantId " +
            "and user.profile.mobilePhoneNo = :mobilePhoneNo")
    EndUser findByProfileMobilePhoneNo(
            @Param("mobilePhoneNo") String mobilePhoneNo,
            @Param("applicationId") String applicationId,
            @Param("tenantId") String tenantId);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.owner.id = :tenantId " +
            "and user.profile.mobilePhoneNo = :mobilePhoneNo")
    EndUser findByProfileMobilePhoneNo(
            @Param("mobilePhoneNo") String mobilePhoneNo,
            @Param("tenantId") String tenantId);

    @Query("select user from APP_USER user join user.applicationEndUsers a where (a.pk.application.id = :applicationId) and user.profile.mobilePhoneNo = :mobilePhoneNo")
    EndUser findByApplicationAndMobileNo(@Param("applicationId") String applicationId, @Param("mobilePhoneNo") String mobilePhoneNo);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.id = :applicationId " +
            "and a.pk.application.owner.id = :tenantId " +
            "and user.profile.gender = :gender")
    Page<EndUser> findByProfileGender(
            @Param("gender") Gender gender,
            @Param("applicationId") String applicationId,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("select user from APP_USER user join user.applicationEndUsers a " +
            "where a.pk.application.owner.id = :tenantId " +
            "and user.profile.gender = :gender")
    Page<EndUser> findByProfileGender(
            @Param("gender") Gender gender,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    @Modifying
    @Query("update APP_USER user set user.credential.password = ?1 where user.id = ?2")
    int updateCredentialPassword(String newPassword, String id);

    @Query("select user from APP_USER user join user.applicationEndUsers a where a.pk.application.owner.id = :tenantId")
    Page<EndUser> findAllEndUsersForTenant(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("select user.status as status, COUNT(user.status) from APP_USER user join user.applicationEndUsers a " +
      "where a.pk.application.owner.id = :tenantId " +
      "GROUP BY user.status")
    List<Object[]> findCountsEndUserByStatusForTenant(@Param("tenantId") String tenantId);
}
