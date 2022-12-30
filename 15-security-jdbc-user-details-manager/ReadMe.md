# Spring Security With Database

> ### JdbcUserDetailsManager

- RememberMe 동작 오류

```java
@Bean
public UserDetailsService userDetailsService(DataSource dataSource) {
    JdbcDaoImpl jdbcDao = new JdbcDaoImpl();
    jdbcDao.setDataSource(dataSource);
    jdbcDao.setEnableAuthorities(false);
    jdbcDao.setEnableGroups(true);
    jdbcDao.setUsersByUsernameQuery(
            "SELECT login_id, passwd, true FROM USERS WHERE login_id = ?"
    );
    jdbcDao.setGroupAuthoritiesByUsernameQuery(
            "SELECT u.login_id, g.name, p.name " +
                    "FROM " +
                    "users u JOIN groups g ON u.group_id = g.id " +
                    "LEFT JOIN group_permission gp ON g.id = gp.group_id " +
                    "JOIN permissions p ON p.id = gp.permission_id " +
                    "WHERE " +
                    "u.login_id = ?"
    );
    return jdbcDao;
}
```

- RememberMe 동작 성공

```java
@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
  auth.jdbcAuthentication()
    .dataSource(dataSource)
    .usersByUsernameQuery(
      "SELECT " +
        "login_id, passwd, true " +
      "FROM " +
        "USERS " +
      "WHERE " +
        "login_id = ?"
    )
    .groupAuthoritiesByUsername(
      "SELECT " +
        "u.login_id, g.name, p.name " +
      "FROM " +
        "users u JOIN groups g ON u.group_id = g.id " +
        "LEFT JOIN group_permission gp ON g.id = gp.group_id " +
        "JOIN permissions p ON p.id = gp.permission_id " +
      "WHERE " +
        "u.login_id = ?"
    )
    .getUserDetailsService().setEnableAuthorities(false)
  ;
}
```

- JdbcDaoImpl 객체를 Bean으로 등록
    - enableGroups
        - Group-based Access Control 활용시 : true
        - groupAuthoritiesByUsername 쿼리 정의시 자동으로 true 설정
    - enableAuthorities
        - Group-based Access Control 활용시 : false