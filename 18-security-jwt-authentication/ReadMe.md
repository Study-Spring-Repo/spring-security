# JwtAuthenticationToken, JwtAuthenticationProvider

> ### JwtAuthentication

- Jwt 전용 Authentication 인터페이스 구현체
- 기존에는 Spring Security에서 제공하는 UsernamePasswordAuthenticationToken을 사용했다.
- UsernamePasswordAuthenticationToken 클래스에서 인증된 사용자 principal으로 org.springframework.security.core.userdetails.User를 사용했다.
  - JwtAuthentication 클래스로 교체한다.

```java
public class JwtAuthentication {

    public final String token;

    public final String username;

    public JwtAuthentication(String token, String username) {
        checkArgument(!token.isEmpty(), "token must be provided");
        checkArgument(!username.isEmpty(), "username must be provided");


        this.token = token;
        this.username = username;
    }

    private void checkArgument(boolean isTrue, String message) {
        if (!isTrue) {
            throw new IllegalArgumentException(message);
        }
    }

    // ...
}
```


> ### JwtAuthenticationToken

- JWT 인증 처리를 명확하게 하기 위해 JwtAuthenticationToken을 추가한다.
  - 이로써 UsernamePasswordAuthenticationToken은 이용하지 않는다.

```java
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;

    private String credentials;

    public JwtAuthenticationToken(String principal, String credentials) {
        super(null);
        super.setAuthenticated(false);

        this.principal = principal;
        this.credentials = credentials;
    }

    JwtAuthenticationToken(Object principal, String credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        setAuthenticated(true);

        this.principal = principal;
        this.credentials = credentials;
    }

    // ...
}
```

> ### JwtAuthenticationProvider

- JwtAuthenticationToken 타입을 처리해줄 AuthenticationProvider 구현체가 필요하다.


- UsernamePasswordAuthenticationToken 타입 처리
  - AuthenticationProvider 인터페이스
    - DaoAuthenticationProvider 클래스
      - UserDetailsService 인터페이스에 의존


- JwtAuthenticationToken 타입을 처리할 수 있는 JwtAuthenticationProvider 추가
- JwtAuthenticationProvider
  - JwtAuthenticationToken 타입을 처리할 수 있다.
  - UserService 클래스를 이용해 로그인 처리, JWT 토큰 생성
    - UserDetailsService 인터페이스를 구현하지 않는다.
  - 인증 완료된 사용자의 JwtAuthenticationToken 반환
    - principal : JwtAuthentication 객체
    - details : 내가 정의한 User

```java
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final Jwt jwt;

    private final UserService userService;

    public JwtAuthenticationProvider(Jwt jwt, UserService userService) {
        this.jwt = jwt;
        this.userService = userService;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (JwtAuthenticationToken.class.isAssignableFrom(authentication));
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
        return processUserAuthentication(
                String.valueOf(jwtAuthenticationToken.getPrincipal()),
                jwtAuthenticationToken.getCredentials()
        );
    }

    private Authentication processUserAuthentication(String principal, String credential) {
        try {
            User user = userService.login(principal, credential);
            List<GrantedAuthority> authorities = user.getGroup().getAuthorities();
            String token = getToken(user.getLoginId(), authorities);
            JwtAuthenticationToken authenticated =
                    new JwtAuthenticationToken(
                            new JwtAuthentication(token, user.getLoginId()),
                            null,
                            authorities);
            authenticated.setDetails(user);
            return authenticated;
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException(e.getMessage());
        } catch (Exception e) {
            throw new AuthenticationServiceException(e.getMessage(), e);
        }
    }

    // ...
}
```