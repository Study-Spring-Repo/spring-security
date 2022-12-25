# Spring Security 인증 이벤트

- 인증 성공, 실패시 관련 ApplicationEvent가 발생한다.
- 해당 event에 관심있는 Component는 event를 구독할 수 있다.


- 이벤트 모델 사용시 주의점
  - 스프링의 이벤트 모델은 동기적이다.
  - 이벤트를 구독하는 리스너의 처리 지연은 발생시킨 요처의 응답 지연에 직접적인 영향을 미친다.


- 이벤트 모델을 사용하는 이유 ?
  - 이벤트 모델은 Component 간의 느슨한 결합을 유지하는데 도움을 준다.


> ### AuthenticationEventPublisher

- `AuthenticationEventPublisher`
  - 인증 성공, 실패가 발생했을 때 이벤트를 전달하기 위한 Event Publisher Interface
  - 기본 구현체로 `DefaultAuthenticationEventPublisher` 클래스가 사용된다.

```java
public interface AuthenticationEventPublisher {

	void publishAuthenticationSuccess(Authentication authentication);

	void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication);

}
```


> ### 이벤트 종류

- `AuthenticationSuccessEvent`
  - 로그인 성공 이벤트
- `AbstractAuthenticationFailureEvent`
  - 로그인 실패 이벤트
  - 추상 클래스이다.
    - 실패 이유마다 다양한 구체 클래스가 있다.
      - BadCredentialsException / AuthenticationFailureBadCredentialsEvent
      - UsernameNotFoundException / AuthenticationFailureBadCredentialsEvent
      - AccountExpiredException / AuthenticationFailureExpiredEvent
      - ProviderNotFoundException / AuthenticationFailureProviderNotFoundEvent
      - DisabledException / AuthenticationFailureDisabledEvent
      - LockedException / AuthenticationFailureLockedEvent
      - AuthenticationServiceException / AuthenticationFailureServiceExceptionEvent
      - CredentialsExpiredException / AuthenticationFailureCredentialsExpiredEvent
      - InvalidBearerTokenException / AuthenticationFailureBadCredentialsEvent

  
> ### 이벤트 리스너

- `@EventListener` 이용하여 리스너 등록
- Spring 이벤트 모델은 동기적이다.
  - 지연처리가 될 수 있다.
  - 해결 방법
    - `@EnableAsync`로 비동기 처리를 활성화한다.
      - `@Async`을 사용해 이벤트 리스너를 비동기로 변경할 수 있다.