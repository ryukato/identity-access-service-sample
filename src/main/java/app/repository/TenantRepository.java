package app.repository;

import app.domain.Application;
import app.domain.EndUser;
import app.domain.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {

    Page<Tenant> findAll(Pageable pageable);

    Tenant findById(String id);

    Tenant findByEmail(String email);

    @Query("select t from Tenant t where t.email like %:email%")
    Page<Tenant> findByEmailLike(@Param("email") String email, Pageable pageable);

    Tenant findByLoginCredentialAccount(String account);

    @Query("select t from Tenant t where t.loginCredential.account like %:account%")
    Page<Tenant> findByLoginCredentialAccountLike(@Param("account") String account, Pageable pageable);

    Tenant findByApiKeyInformationApiKey(String apiKey);

    @Query("select apps from TENANT_APPLICATION apps where apps.owner.id = :id")
    Page<Application> findApplicationsByTenantId(@Param("id") String id, Pageable pageable);

    @Query("select user from APP_USER user where user.tenantId = :id")
    Page<EndUser> findEndUsersByTenantId(@Param("id") String id, Pageable pageable);
}
