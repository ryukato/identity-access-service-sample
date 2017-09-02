package app.util;

import app.domain.Tenant;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BeanCopyUtilTest {
    @Test
    public void copyNonNullProperties() {
        Tenant sourceTenant = TenantTestUtil.createTestTenant();
        sourceTenant.setEmail(null);
        Tenant targetTenant = new Tenant();
        targetTenant.setId("testId");
        targetTenant.setEmail("targetTenant@test.com");
        BeanCopyUtil.copyNonNullProperties(sourceTenant, targetTenant);
        assertEquals("Tenant company name should equal", sourceTenant.getCompanyName(), targetTenant.getCompanyName());
        assertEquals("Tenant company name should equal", sourceTenant.getLoginCredential().getAccount(), targetTenant.getLoginCredential().getAccount());
        assertEquals("Tenant company name should equal", sourceTenant.getLoginCredential().getPassword(), targetTenant.getLoginCredential().getPassword());
        assertEquals("Source tenant email have to be null", null, sourceTenant.getEmail());
        assertEquals("Tenant email haven't changed", "targetTenant@test.com", targetTenant.getEmail());
        assertEquals("Tenant id haven't changed", "testId", targetTenant.getId());
    }
}
