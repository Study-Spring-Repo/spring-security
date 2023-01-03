# 정보 확인 API, @AuthenticationPrinciple

- 인증 처리
  - AuthenticationManager를 그대로 사용한다.


- 인증 요청
  - JwtAuthenticationToken 객체를 만들어 AuthenticationManager를 통해 처리할 수 있다.


> ### JwtAuthenticationFilter 수정

  - JWT 토큰을 검증한다.
  - 디코딩하고 JwtAuthenticationToken 객체를 생성하여 UsernamePasswordAuthenticationToken을 대체한다.
    - principal 필드
      - JwtAuthentication 객체
    - details 필드
      - WebAuthenticationDetails 객체
  - SecurityContextHolder.getContext().setAuthentication 메서드를 호출한다.
    - JwtAuthenticationToken 객체 참조를 전달한다.

```java
public class JwtAuthenticationFilter extends GenericFilterBean {

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
          throws IOException, ServletException {
    // ... 
    if (!username.isBlank() && authorities.size() > 0) {
            JwtAuthenticationToken authentication
                    = new JwtAuthenticationToken(new JwtAuthentication(token, username), null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
          }
   // ...
}
```

> ### @AuthenticationPrincipal

-  내 정보 조회 API를 구현한다.


- @AuthenticationPrincipal
  - Authentication 인터페이스 구현체에서 principal 필드를 추출하는 어노테이션이다.
  - JwtAuthenticationToken 타입이 사용되었다면 JwtAuthentication 객체를 의미한다.
  - 적절한 권한이 없다면 인가 처리 과정에서 예외가 발생한다.

```java
@GetMapping(path = "/user/me")
public UserDto me(@AuthenticationPrincipal JwtAuthentication authentication) {
    return userService.findByLoginId(authentication.username)
            .map(user -> new UserDto(authentication.token, authentication.username, user.getGroup().getName()))
            .orElseThrow(() -> new IllegalArgumentException("Could not found user for " + authentication.username));
}
```

> ### AuthenticationPrincipalArgumentResolver를 통해 처리된다.

- AuthenticationPrincipalArgumentResolver
  - HandlerMethodArgumentResolver 인터페이스 구현체이다.

> ### HandlerMethodArgumentResolver

- Controller 메서드 호출에 필요한 파라미터를 바인딩 시키기위한 인터페이스
- supportParameter 메서드가 true를 반환하는 경우 resolverArgument 메서드가 실행되어 Controller 메서드 호출에 필요한 파라미터를 만든다.