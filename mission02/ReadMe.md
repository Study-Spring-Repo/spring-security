# 02. Spring Security Architecture

> ### AnonymousAuthenticationFilter

- `AnonymousAuthenticationFilter`
  - 해당 필터에 요청이 도달할 때까지 사용자가 인증되지 않았다면
  - 사용자를 `null` 대신 `Anonymous` 인증 타입으로 표현한다.
    - 더 구체적으로 타입을 확인할 수 있도록 도와준다.

```java
public class AnonymousAuthenticationFilter extends GenericFilterBean implements InitializingBean {
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      SecurityContextHolder.getContext().setAuthentication(this.createAuthentication((HttpServletRequest) req));
      if (this.logger.isTraceEnabled()) {
        this.logger.trace(LogMessage.of(() -> {
          return "Set SecurityContextHolder to " + SecurityContextHolder.getContext().getAuthentication();
        }));
      } else {
        this.logger.debug("Set SecurityContextHolder to anonymous SecurityContext");
      }
    } else if (this.logger.isTraceEnabled()) {
      this.logger.trace(LogMessage.of(() -> {
        return "Did not set SecurityContextHolder since already authenticated " + SecurityContextHolder.getContext().getAuthentication();
      }));
    }

    chain.doFilter(req, res);
  }

  /**
   * 익명 인증 타입 토큰을 생성하는 것을 볼 수 있다.
   */
  protected Authentication createAuthentication(HttpServletRequest request) {
    AnonymousAuthenticationToken token = new AnonymousAuthenticationToken(this.key, this.principal, this.authorities);
    token.setDetails(this.authenticationDetailsSource.buildDetails(request));
    return token;
  }
    
  ...
}
``` 
