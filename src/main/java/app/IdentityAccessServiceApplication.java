package app;

import app.repository.ApplicationRepository;
import app.repository.EndUserRepository;
import app.repository.TenantRepository;
import app.service.TenantService;
import app.util.ClientDetailsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.orm.jpa.vendor.HibernateJpaSessionFactoryBean;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/*
 // TODO: Create Events such as EndUserCreated, EndUserRegistered, EndUserActivated.
 // TODO: Add unit test, it test (from controller -> repository)
 */
@EntityScan(
		basePackageClasses = {Jsr310JpaConverters.class},
		basePackages = {"com.eoe.domain"}
)
@SpringBootApplication
public class IdentityAccessServiceApplication {
	private static final Logger log = LoggerFactory.getLogger(IdentityAccessServiceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(IdentityAccessServiceApplication.class, args);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public HibernateJpaSessionFactoryBean sessionFactory() {
		return new HibernateJpaSessionFactoryBean();
	}

	@Bean
	public FilterRegistrationBean registerOpenSessionInViewFilterBean() {
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		OpenEntityManagerInViewFilter filter = new OpenEntityManagerInViewFilter();
		registrationBean.setFilter(filter);
		registrationBean.setOrder(5);
		return registrationBean;
	}


	@RestController
	@RequestMapping("test")
	public static class TestController {
		@RequestMapping(value = "public-test", method = RequestMethod.GET)
		public ResponseEntity<String> test() {
			return ResponseEntity.ok("success");
		}

		@RequestMapping(value = "authorization-code", method = RequestMethod.GET)
		public ResponseEntity<Principal> authorization(Principal principal) {
			return ResponseEntity.ok(principal);
		}

		@RequestMapping(value = "only-ap-manager", method = RequestMethod.GET)
		@Secured("hasRole('AP_MANAGER')")
		public ResponseEntity<Principal> onlyAdmin(Principal principal) {
			return ResponseEntity.ok(principal);
		}
	}

	@Profile({"dev"})
	@Configuration
	public static class InitUserDataCommandLineRunner implements CommandLineRunner {
		@Autowired
		EndUserRepository endUserRepository;

		@Autowired
		ApplicationRepository applicationRepository;

		@Autowired
		private TenantRepository tenantRepository;

		@Autowired
		private TenantService tenantService;

		@Autowired
		PasswordEncoder passwordEncoder;

		@Autowired
		private JdbcClientDetailsService jdbcClientDetailsService;

		@Autowired
		private ClientDetailsFactory<app.domain.Application> clientDetailsFactory;


		@Transactional
		@Override
		public void run(String... strings) throws Exception {
			prepareTestTenant();
			prepareTestApplications();
			prepareTenantApplications();
			prepareTestUsers();
			prepareApplicationEndUsersForDev();
		}

		private void prepareTestTenant() {
			app.domain.Tenant tenant = new app.domain.Tenant();
			tenant.setCompanyName("test tenant company");
			tenant.setEmail("test_tenant1@test.com");
			tenant.setStatus(app.domain.Tenant.Status.ACTIVE);
			tenant.setLoginCredential(new app.domain.LoginCredential("test_tenant1", "password"));

			app.domain.UserProfile tenantProfile = new app.domain.UserProfile();
			tenantProfile.setFirstName("tenant_firstName");
			tenantProfile.setLastName("tenant_lastName");
			tenantProfile.setMiddleName("tenant_middleName");
			app.domain.Address address = new app.domain.Address();
			address.setZipcode("123-456");
			address.setMainAddress("tenant_main address");
			address.setDetailAddress("tenant_detail address");
			tenantProfile.setGender(app.domain.Gender.MALE);
			tenantProfile.setBirthDate("2017-01-01");
			tenantProfile.setCountry("Korea");
			tenantProfile.setLanguage("Korean");
			tenantProfile.setMobilePhoneNo("010-1111-0000");
			tenantProfile.setNickName("tenant_nickName");
			tenantProfile.setTimezone("GMT+9");
			tenantProfile.setAddress(address);
			tenant.setProfile(tenantProfile);

			tenantService.createTenant(tenant);

			app.domain.Tenant spTenant = new app.domain.Tenant();
			spTenant.setCompanyName("test tenant service-portal company");
			spTenant.setEmail("service-portal@test.com");
			spTenant.setStatus(app.domain.Tenant.Status.ACTIVE);
			spTenant.setLoginCredential(new app.domain.LoginCredential("service-portal", "secret"));

			app.domain.UserProfile spTenantProfile = new app.domain.UserProfile();
			spTenantProfile.setFirstName("service-portal_firstName");
			spTenantProfile.setLastName("service-portal_lastName");
			spTenantProfile.setMiddleName("service-portal_middleName");
			app.domain.Address address1 = new app.domain.Address();
			address1.setZipcode("123-456");
			address1.setMainAddress("service-portal_main address");
			address.setDetailAddress("service-portal_detail address");
			spTenantProfile.setGender(app.domain.Gender.MALE);
			spTenantProfile.setBirthDate("2017-01-01");
			spTenantProfile.setCountry("Korea");
			spTenantProfile.setLanguage("Korean");
			spTenantProfile.setMobilePhoneNo("010-1111-9999");
			spTenantProfile.setNickName("tenant_nickName");
			spTenantProfile.setTimezone("GMT+9");
			spTenantProfile.setAddress(address1);
			spTenant.setProfile(tenantProfile);

			tenantService.createTenant(spTenant);

			BaseClientDetails clientDetails = (BaseClientDetails) jdbcClientDetailsService.loadClientByClientId("service-portal");
			clientDetails.setClientSecret("secret");
			jdbcClientDetailsService.removeClientDetails("service-portal");
			jdbcClientDetailsService.addClientDetails(clientDetails);
		}

		private void prepareTenantApplications() {
			app.domain.Tenant tenant = tenantRepository.findByLoginCredentialAccount("test_tenant1");
			Page<app.domain.Application> applications = applicationRepository.findByNameAndManagerId("test_application", "test_manager", new PageRequest(0, 1));
			app.domain.Application application = applications.getContent().get(0);
			tenant.getApplications().add(application);
			application.setOwner(tenant);
			applicationRepository.save(application);
			tenantRepository.save(tenant);
		}

		private void prepareApplicationEndUsersForDev() {
			Page<app.domain.Application> applications = applicationRepository.findByNameAndManagerId("test_application", "test_manager", new PageRequest(0, 1));
			app.domain.Application application = applications.getContent().get(0);
			app.domain.EndUser endUser = endUserRepository.findByCredentialAccount("test_user");
			app.domain.ApplicationEndUser applicationEndUser = new app.domain.ApplicationEndUser();
			applicationEndUser.setApplication(application);
			applicationEndUser.setEndUser(endUser);
			application.getApplicationEndUsers().add(applicationEndUser);
			endUser.getApplicationEndUsers().add(applicationEndUser);
//			applicationRepository.save(application);
		}

		private void prepareTestApplications() {
			try {
				applicationRepository.deleteAll();
				log.info("prepareTestApplications: start");
				app.domain.Application application = new app.domain.Application();
				application.setId("foo");
				application.setManagerId("test_manager");
				application.setName("test_application");
				application.setStatus(app.domain.Application.ApplicationStatus.ACTIVE);
				application.setDisabledNewUser(false);
				application.setApiKey("test_api_key");
//				application.setCreatedAt(new Date());
//				application.setCreatedBy("TEST_ADMIN");

				log.info("prepareTestApplications completed: {}", application);

				app.domain.Application createdApp = applicationRepository.save(application);

				jdbcClientDetailsService.addClientDetails(clientDetailsFactory.createFrom(createdApp));

			}catch (Throwable t) {
				System.err.println(t.getMessage());
			}
		}

		private void prepareTestUsers() {
			try {
				endUserRepository.deleteAll();
				log.info("prepareTestUsers: start");
				app.domain.EndUser endUser = new app.domain.EndUser();
				endUser.setStatus(app.domain.EndUserStatus.CREATED);
				endUser.setEmail("test_user@test.com");
				app.domain.LoginCredential loginCredential = new app.domain.LoginCredential();
				loginCredential.setAccount("test_user");
				loginCredential.setPassword(passwordEncoder.encode("test"));
				endUser.setCredential(loginCredential);

				app.domain.UserProfile endUserProfile = new app.domain.UserProfile();
				endUserProfile.setFirstName("firstName");
				endUserProfile.setLastName("lastName");
				endUserProfile.setMiddleName("middleName");
				app.domain.Address address = new app.domain.Address();
				address.setZipcode("123-456");
				address.setMainAddress("main address");
				address.setDetailAddress("detail address");
				endUserProfile.setGender(app.domain.Gender.MALE);
				endUserProfile.setBirthDate("2017-01-01");
				endUserProfile.setCountry("Korea");
				endUserProfile.setLanguage("Korean");
				endUserProfile.setMobilePhoneNo("010-1111-2222");
				endUserProfile.setNickName("nickName");
				endUserProfile.setTimezone("GMT+9");
				endUserProfile.setAddress(address);
				endUser.setProfile(endUserProfile);

				app.domain.Tenant tenant = tenantRepository.findByLoginCredentialAccount("test_tenant1");
				endUser.setTenantId(tenant.getId());

				log.info("prepareTestUsers completed: {}", endUser);

				endUserRepository.save(endUser);
			}catch (Throwable t) {
				System.err.println(t.getMessage());
			}
		}
	}
}
