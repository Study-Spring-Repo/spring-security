# WebAsyncManagerIntegrationFilter

> ### WebAsyncManagerIntegrationFilter

- SecurityContext는 ThreadLocal 변수를 이용하고 있다.
- 다른 쓰레드에서는 SecurityContext에서는 참조할 수 없어야 한다.


- `WebAsyncManagerIntegrationFilter`
  - MVC Async Request가 처리될 때, 쓰레드간 `SecurityContext`를 공유할 수 있게 해준다.
  - `SecurityContextCallableProcessingInterceptor` 클래스를 이용한다.

> ### SecurityContextCallableProcessingInterceptor

- `beforeConcurrentHandling()`
- HTTP 요청을 처리하고 있는 WAS 쓰레드에서 실행한다.
  - 해당 메서드 구현의 `SecurityContextHolder.getContext()` 부분은 `ThreadLocal`의 `SecurityContext` 정상적으로 참조한다.
  - 즉, `ThreadLocal`의 `SecurityContext` 객체를 `SecurityContextCallableProcessingInterceptor` 클래스 멤버변수에 할당한다.

  
- `preProcess()`, `postProcess()`
  - 별도의 스레드에서 실행한다.


- Spring MVC Async Request 처리에서만 적용된다. (Controller 메서드)
- @Async 어노테이션을 추가한 Service 계층 메서드에는 해당되지 않는다.

```java
@Controller
public class SimpleController {

  public final Logger log = LoggerFactory.getLogger(getClass());

  private final SimpleService simpleService;

  public SimpleController(SimpleService simpleService) {
    this.simpleService = simpleService;
  }

	// ...
  @GetMapping(path = "/someMethod")
  @ResponseBody
  public String someMethod() {
    log.info("someMethod started.");
    simpleService.asyncMethod();
    log.info("someMethod completed.");
    return "OK";
  }

}
```

```java
@Service
public class SimpleService {

  public final Logger log = LoggerFactory.getLogger(getClass());

  @Async
  public String asyncMethod() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User principal = authentication != null ? (User) authentication.getPrincipal() : null;
    String name = principal != null ? principal.getUsername() : null;
    log.info("asyncMethod result: {}", name);
    return name;
  }

}
```

```java
public final class SecurityContextCallableProcessingInterceptor extends CallableProcessingInterceptorAdapter {

	private volatile SecurityContext securityContext;

    //... 생략 ...

	@Override
	public <T> void beforeConcurrentHandling(NativeWebRequest request, Callable<T> task) {
		if (this.securityContext == null) {
			setSecurityContext(SecurityContextHolder.getContext());
		}
	}

	@Override
	public <T> void preProcess(NativeWebRequest request, Callable<T> task) {
		SecurityContextHolder.setContext(this.securityContext);
	}

	@Override
	public <T> void postProcess(NativeWebRequest request, Callable<T> task, Object concurrentResult) {
		SecurityContextHolder.clearContext();
	}

	//...

}
```

> ### SecurityContextHolderStrategy

- `SecurityContextHolderStrategy` 설정값을 `MODE_INHERITABLETHREADLOCAL` 으로 변경한다.

```java
public class WebSecurityConfigure extends WebSecurityConfigurerAdapter {

  // ...

  public WebSecurityConfigure() {
    SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
  }

  // ...
}
```

- 다른 쓰레드에서도 SecurityContext를 참조할 수 있게 된다.
- `SecurityContextHolderStrategy` 인터페이스 구현체를 기본값 `ThreadLocalSecurityContextHolderStrategy`에서 `InheritableThreadLocalSecurityContextHolderStrategy`으로 변경한다.
- `SecurityContext` 저장 변수를 `ThreadLocal`에서 `InheritableThreadLocal` 타입으로 변경하게 된다.


- `InheritableThreadLocal`
  - 부모 쓰레드가 생성한 ThreadLocal 변수를 자식 쓰레드에서 참조할 수 있다.

> ### DelegatingSecurityContextAsyncTaskExecutor

- `MODE_INHERITABLETHREADLOCAL을` 설정하여 이용하는 방법을 권장하지는 않는다.
  - Pooling 처리된 `TaskExecutor`와 함께 사용시 ThreadLocal의 clear 처리가 제대로되지 않아 문제가 될 수 있다.
  - Pooling 되지 `TaskExecutor`와 함께 사용해야 한다.
- 내부적으로 `Runnable`을 `DelegatingSecurityContextRunnable` 타입으로 wrapping 처리한다.
- `DelegatingSecurityContextRunnable` 객체 생성자에서 `SecurityContextHolder.getContext()` 메서드를 호출하여 `SecurityContext` 참조를 획득한다.