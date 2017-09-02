package app.repository;

import app.domain.ApiKeyInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantApiKeyRepository extends JpaRepository<ApiKeyInformation, String> {

}
