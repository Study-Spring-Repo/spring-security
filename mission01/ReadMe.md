# 02. Spring Security Architecture

> ### 02-01. Conceptual Architecture

- `Spring Security`
  - 웹 요청을 가로채어 사용자를 인증한다.
  - 인증된 사용자가 적절한 권한을 지니고 있는지 확인한다.
- `AuthenticationManager`
  - 사용자 인증 관련 처리
- `AccessDecisionManager`
  - 사용자가 보호받는 리소스에 접근할 수 있는 권한이 있는지 확인

> ### 02-02. FilterChainProxy

> `Spring Security 필터 체인`
  - Spring Security의 실제 구현은 서블릿 필터를 통해 이루어진다.


>`서블릿 필터`
  - `javax.servlet.Filter`
  - 웹 요청을 가로챈다.
    - 전처리를 수행한다.
    - 후처리를 수행한다.
    - 요청 자체를 리다이렉트한다.


> `FilterChainProxy`
  - FilterChainProxy의 세부 내용은 `WebSecurityConfigurerAdapter` 추상 클래스를 상속하는 구현체에서 설정한다.
    - `@EnableWebSecurity`도 사용한다.
  - 웹 요청은 이러한 필터 체인을 차례로 통과한다.
    - 웹 요청은 모든 필터를 통과하지만, 모든 필터가 동작하지는 않는다.
    - 각 필터는 웹 요청에 따라 동작 여부를 결정할 수 있다.
    - 동작할 필요가 없다면 다음 필터로 웹 요청을 즉시 넘긴다.
  - 요청을 처리하고 응답을 반환하면 필터 체인 호출 스택은 모든 필터에 대해 역순으로 진행한다.
  - `springSecurityFilterChain` 이라는 이름으로 Bean 등록된다.


> 웹 요청,  `FilterChainProxy`로 전달 되는 법
  - 웹 요청을 수신한 서블릿 컨테이너는
    - 해당 요청을 `DelegatingFilterProxy`로 전달한다.
    - `DelegatingFilterProxy Bean`은 `SecurityFilterAutoConfiguration 클래스에서 자동으로 등록된다.
  - `DelegatingFilterProxy`는 실제적으로 웹 요청을 처리할 Target Filter Bean을 지정해야 한다.
    - Target Filter Bean ?? => `FilterChainProxy` 이다.
      <img alt="img.png" height="500" src="img.png" width="700"/>


>`FilterChainProxy`를 구성하는 `Filter` 목록
  - `DelegatingFilterProxy Bean`
    - SecurityFilterAutoConfiguration 클래스에서 자동으로 등록된다.
  - `ChannelProcessingFilter`
    - 웹 요청이 어떤 프로토콜로 (http 또는 https) 전달되어야 하는지 처리
  - `SecurityContextPersistenceFilter`
    - SecurityContextRepository를 통해
    - SecurityContext를 Load/Save 처리
  - `LogoutFilter`
    - 로그아웃 URL로 요청을 감시하여 매칭되는 요청이 있으면 해당 사용자를 로그아웃 시킴
  - `UsernamePasswordAuthenticationFilter`
    - ID/비밀번호 기반 Form 인증 요청 URL(기본값: /login) 을 감시하여 사용자를 인증함
  - `DefaultLoginPageGeneratingFilter`
    - 로그인을 수행하는데 필요한 HTML을 생성함
  - `RequestCacheAwareFilter`
    - 로그인 성공 이후 인증 요청에 의해 가로채어진 사용자의 원래 요청으로 이동하기 위해 사용됨
  - `SecurityContextHolderAwareRequestFilter`
    - 서블릿 3 API 지원을 위해 HttpServletRequest를 HttpServletRequestWrapper 하위 클래스로 감쌈
  - `RememberMeAuthenticationFilter`
    - 요청의 일부로 remember-me 쿠키 제공 여부를 확인하고,
    - 쿠키가 있으면 사용자 인증을 시도함
  - `AnonymousAuthenticationFilter`
    - 해당  인증 필터에 도달할때까지 사용자가 아직 인증되지 않았다면, 익명 사용자로 처리하도록 함
  - `ExceptionTranslationFilter`
    - 요청을 처리하는 도중 발생할 수 있는 예외에 대한 라우팅과 위임을 처리함
  - `FilterSecurityInterceptor`
    -   접근 권한 확인을 위해 요청을 `AccessDecisionManager`로 위임

