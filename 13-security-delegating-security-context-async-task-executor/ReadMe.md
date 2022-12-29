# DelegatingSecurityContextAsyncTaskExecutor

> ### DelegatingSecurityContextAsyncTaskExecutor
- `MODE_INHERITABLETHREADLOCAL을` 설정하여 이용하는 방법을 권장하지는 않는다.
    - Pooling 처리된 `TaskExecutor`와 함께 사용시 ThreadLocal의 clear 처리가 제대로되지 않아 문제가 될 수 있다.
    - Pooling 되지 `TaskExecutor`와 함께 사용해야 한다.
- 내부적으로 `Runnable`을 `DelegatingSecurityContextRunnable` 타입으로 wrapping 처리한다.
- `DelegatingSecurityContextRunnable` 객체 생성자에서 `SecurityContextHolder.getContext()` 메서드를 호출하여 `SecurityContext` 참조를 획득한다.


```java
@Configuration
@EnableWebSecurity
public class WebSecurityConfigure extends WebSecurityConfigurerAdapter {
    
    // ...

    @Bean
    @Qualifier("myAsyncTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix("my-executor-");
        return executor;
    }

    @Bean
    @Qualifier("myAsyncTaskExecutor")
    public DelegatingSecurityContextAsyncTaskExecutor taskExecutor(
            @Qualifier("myAsyncTaskExecutor") AsyncTaskExecutor delegate
    ) {
        return new DelegatingSecurityContextAsyncTaskExecutor(delegate);
    }
    
    // ...
}
```