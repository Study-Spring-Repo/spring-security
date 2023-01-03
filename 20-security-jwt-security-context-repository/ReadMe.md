# JwtSecurityContextRepository

> ### JwtAuthenticationFilter 구현 대체

- JwtAuthenticationFilter의 핵심 역할
  - HTTP 요청 헤더에서 JWT 토큰을 확인하고, 검증하여 SecurityContext를 생성한다.

```java
public class JwtAuthenticationFilter extends GenericFilterBean {

    // JWT 토큰 가져옴
    String token = getToken(request);
    // JWT 디코딩
    Jwt.Claims claims = verify(token);
    // JwtAuthenticationToken 객체를 생성하고 SecurityContext에 참조를 넘겨줌
    JwtAuthenticationToken authentication = new JwtAuthenticationToken(new JwtAuthentication(token, username), null, authorities);
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().
    setAuthentication(authentication);
}
```

> ### SecurityContextPersistenceFilter

- SecurityContextPersistenceFilter
  - JwtAuthenticationFilter와 비슷한 역할을 하는 Spring Security의 기본 필터
  - SecurityContextRepository에서 SecurityContext를 읽어온다.
    - SecurityContextRepository는 HTTP Request에서 필요한 데이터를 얻어 이용한다.


- JwtSecurityContextRepository
  - HTTP Request에서 필요한 데이터를 얻을 때 JWT 토큰을 이용한다.
  - JwtAuthenticationFilter 구현의 대부분을 JwtSecurityContextRepository 구현으로 가져올 수 있다.
  - SecurityContextRepository 인터페이스에서 요구하는 메서드 구현을 모두 완료할 수 있어야 한다.
    - saveContext 메서드
      - SecurityContext를 저장하고, 필요할 때 다시 읽어 올 수 있어야 한다.
    - containsContext 메서드
      - HTTP Request가 SecurityContext를 포함하고 있는지 여부를 확인할 수 있다.


- SecurityContextPersistenceFilter에서 JwtSecurityContextRepository를 사용하도록 설정
  - JwtAuthenticationFilter 역할을 SecurityContextPersistenceFilter에서 수행하게 된다.


- SecurityContextRepository 인터페이스 구현체를 SessionManagementFilter에서도 사용한다.

> ### SecurityContextRepository 설정

- securityContext()의 securityContextRepository 메서드를 통해 커스텀 SecurityContextRepository 구현체를 등록한다.
- SecurityContextPersistenceFilter, SessionManagementFilter 모두 커스텀 SecurittyContextRepository 구현체를 사용하게 된다.

```java
@Configuration
@EnableWebSecurity
public class WebSecurityConfigure extends WebSecurityConfigurerAdapter {
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
            .securityContext()
            .securityContextRepository(securityContextRepository())
            .and()
    // ...
  }
}
```

> ### JwtAuthenticationFilter 구현 VS SecurityContextRepository 커스텀 구현

- JwtAuthenticationFilter
  - HTTP header에서 JWT Token을 추출하고 검증하여 SecurityContext를 생성한다.
  - Security Filter 체인 상에서 어디에 위치하는지가 중요
    - SecurityContextPersistenceFilter 바로 뒤에 또는 UsernamePasswordAuthenticationFilter 필터 전후로 위치하면 적당하다.


- SecurityContextRepository 커스텀 구현
  - JwtAuthenticationFilter 구현과 유사하다.
  - SecurityContextRepository 인터페이스에 맞추어 메서드 구현이 필요하다.
    - saveContext, containsContext
  - SecurityContextPersistenceFilter, SessionManagementFilter 2개의 필터에서 SecurityContextRepository 구현이 어떻게 사용되는지 잘 알고 있어야 한다.


- SecurityContextRepository 인터페이스 커스텀 구현 방식이 추가적으로 고려할 내용이 많다.
  - Spring Security 전반에 걸쳐 끼치는 영향이 크다.


- SessionManagementFilter를 사용할 경우 SecurityContextRepository 메서드 구현 방법에 따라 적절한 설정이 필요하다. 