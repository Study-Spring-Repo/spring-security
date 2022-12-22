# 03-mission-remember-me

> ### RememberMeAuthenticationFilter :: FILTER

- 인증되지 않은 사용자의 HTTP 요청이 `remember-me 쿠키`를 갖고 있다면, 사용자를 자동으로 인증 처리한다.
  - key - `remember-me`
    - 쿠키에대한 고유 식별 키
      - 미입력시 랜덤 텍스트
  - `rememberMeParameter`
    - remember-me 쿠키 파라미터명 (default : remember-me)
  - `tokenValiditySSeconds`
    - 쿠키 만료 시간 (초단위)
  - `alwaysRemember`
    - 항상 remember-me를 활성화 시킨다. (default : false)


WebSecurityConfig (Remember-Me 설정 예시)

```java
@Configuration
@EnableWebSecurity
public class WebSecurityConfigure extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
            // ... 생략 ...
            .rememberMe()
            .key("my-remember-me")
            .rememberMeParameter("remember-me")
            .tokenValiditySeconds(300)
            .alwaysRemember(false)
            .and()
    // ... 생략 ...

  }
}
```

> ### RememberMeServices :: SERVICES

- 실제 사용자 인증은 RememberMeServices 인터페이스 구현체를 통해 처리된다.
  - `TokenBasedRememberMeServices`
    - MD5 해시 알고리즘 기반 쿠키 검증
  - `PersistentTokenBasedRememberMeServices`
    - 외부 데이터베이스에서 인증에 필요한 데이터를 가져오고 검증한다.
    - 사용자마다 고유의 Series 식별자가 생성된다.
    - 인증시 매번 갱신되는 임의의 토큰 값을 사용한다.
      - 높은 보안성을 제공한다.


> ### RememberMeAuthenticationToken :: TOKEN

- remember-me 기반 `Authentication` 인터페이스 구현체
- `RememberMeAuthenticationToken` 객체는 언제나 인증이 완료된 상태로 존재한다.

> ### RememberMeAuthenticationProvider :: PROVIDER

- `RememberMeAuthenticationProvider`
  - `RememberMeAuthenticationToken` 기반 인증 처리를 위한 `AuthenticationProvider`
  - 앞서 remember-me 설정 시 입력한 key 값을 검증한다.


RememberMeAuthenticationProvider

```java
@Override
public Authentication authenticate(Authentication authentication) throws AuthenticationException {
	if (!supports(authentication.getClass())) {
		return null;
	}
	if (this.key.hashCode() != ((RememberMeAuthenticationToken) authentication).getKeyHash()) {
		throw new BadCredentialsException(this.messages.getMessage("RememberMeAuthenticationProvider.incorrectKey",
				"The presented RememberMeAuthenticationToken does not contain the expected key"));
	}
	return authentication;
}
```

> ### 명시적인 로그인 아이디/비밀번호 기반 인증 사용, 권한 구분

- **"remember-me 인증 기반"과 "로그인 아이디/비밀번호 기반 인증" 결과가 다르다.**
  - remember-me 기반 인증 결과
    - `RememberMeAuthenticationToken`
  - 로그인 아이디/비밀번호 기반 인증 결과
    - `UsernamePasswordAuthenticationToken`


- remember-me 기반 인증은 로그인 기반 인증보다 보안상 약한 인증이다.
- 동일하게 인증된 사용자라해도 권한을 분리할 수 있다.


- `isFullyAuthenticated`
  - 명시적인 로그인 아이디/비밀번호 기반으로 인증된 사용자만 접근 가능