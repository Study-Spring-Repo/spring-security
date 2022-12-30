# MISSION 05 - JdbcDaoImpl 설정 JPA로 구현

> ### UserDetailsService 인터페이스 구현체 UserService 클래스  추가

- `org.springframework.security.core.userdetails.User.UserBuilder`를 이용해 반환 객체 `UserDetails`를 생성한다.

```java
@Service
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findByLoginId(username)
      .map(user ->
        User.builder()
          .username(user.getLoginId())
          .password(user.getPasswd())
          .authorities(user.getGroup().getAuthorities())
          .build()
      )
      .orElseThrow(() -> new UsernameNotFoundException("Could not found user for " + username));
  }

}
```

> ### configure(AuthenticationManagerBuilder auth) 오버라이드

- Spring Security에서 `UserService` 객체를 `UserDetailsService` 인터페이스 구현체로 사용할 수 있도록 등록한다.

```java
@Configuration
@EnableWebSecurity
public class WebSecurityConfigure extends WebSecurityConfigurerAdapter {

  private UserService userService;

  @Autowired
  private void setUserService(UserService userService) {
    this.userService = userService;
  }

	@Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userService);
  }

	// ...
}
```