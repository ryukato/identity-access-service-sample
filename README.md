# Identity-Access-Service Sample
IAM(Identity-Access-Management)를 구현한 sample project 입니다. auth server와 resource server가 하나의 project, 즉 하나의 service application에 구현되어 있고 tenant manager를 위한 api들도 모두 포함하고 있다. 

Multi tenant를 기반으로 하고 있지만, 아주 초보적인 model이기때문에 많은 개선이 필요하다. 또한 MSA 형태로 구현하기 위해선, Auth service, Resource service 그리고 tenant manager들이 접속하여 계정 관리를 위한 service의 별도 분리를 해야 한다.

단, OAuth2 기반의 IAM 서비스를 구현하기 위한 참고용 프로젝트로는 활용 가능하다.

## Test
### Using Curl
#### Get AccessToken from Auth-Service
* 아래의 코드 내용은 **client_credentials** grant_type으로 AccessToken을 요청하는 명령어 이다. 아래의 명령어 중, 아래의 항목의 값을 확인하여 수정 후, 명령을 실행한다.
 * username: 로그인할 사용자 id
 * password: 사용자 비밀번호
 * scope: 사용자 권한에 대한 scope (read, write, trust)
 * client_id: 등록된 appplication의 id 혹은 사전에 설정된 client_id 
 * client_secret: 등록된 appplication의 secret 혹은 사전에 설정된 secret
 * redirect_uri: 인증 완료 후, 요청된 주소로 사용자 요청을 전달한다.

* "http://foo:bar@localhost:8080/oauth/token" 부분은 Auth-service가 제공하는 API주소이며, 해당 API를 통해 AccessToken을 발급 받을 수 있다.
```
curl -F "grant_type=client_credentials" -F "username=user" -F "password=test" -F "scope=read" -F "client_id=foo" -F "client_secret=bar" -F "redirect_uri=http://localhost:8081/test/authorization-code" "http://foo:bar@localhost:8080/oauth/token"
```
#### Call API of Resource-Service
* "Authorization:Bearer **9fcaf70a-8f9e-4238-8f9f-71326a7d537a**" 부분이 Bearer방식으로 발급받은 토근을 요청 헤더에 첨부하는 부분이다. bold처리된 부분의 토큰 값을 새로 발급받은 토큰으로 수정하여야 한다.
* "http://localhost:8081/test/" 부분은 Resource server에서 제공하는 API의 주소이다. 호출하고자하는 API에 따라 주소 값이 달라질 수 있다.

```
curl -H "Authorization:Bearer 9fcaf70a-8f9e-4238-8f9f-71326a7d537a" "http://localhost:8081/test/"
```

### Using Httpie

#### Get AccessToken from Auth-Service
##### Password GrantType

```
http -f POST :8080/oauth/token "Authorization: Basic Zm9vOmJhcg==" grant_type=password username=test_user password=test
```

##### Client Gredentials Grant Type

```
http -f POST http://service-portal:secret@localhost:8080/oauth/token grant_type=client_credentials client_id=service-portal client_secret=secret
```

```
http -f POST https://identity-access-management.herokuapp.com/oauth/token grant_type=client_credentials client_id=service-portal client_secret=secret "Authorization: Basic c2VydmljZS1wb3J0YWw6c2VjcmV0"
```

#### Call API of Resource-Service

```
http :8081/test "Authorization: Bearer c89c154f-58f1-486d-8a6c-e3d11c283c22"
```

### Run

#### From Terminal
* 아래의 명령어를 실행하기 전에 해당 프로젝트의 경로로 이동한다. 

```
mvn clean spring-boot:run
```

### ETC

#### console log
* Auth-Service 실행 중, **Failed to find access token for token** 메세지는 새로운 accessToken 발행 시, 이미 발행된 accessToken이 없음을 나타내는 메세지이다. 무시해도 상관없다. 

