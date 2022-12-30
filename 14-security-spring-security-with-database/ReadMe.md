# Spring Security With Database

# 데이터베이스를 사용한 인증 적용

- `AuthenticationManager`는 사용자의 인증 처치를 위한 작업을 `AuthenticationProvider`로 위임한다.

- `UsernamePasswordAuthenticationToken` 타입의 인증 요청은 `DaoAuthenticationProvider`가 처리한다.

![img.png](image/img.png)

> ### DaoAuthenticationProvider

- 데이터베이스에서 사용자 인증 정보를 조회하는 작업을 `UserDetailService` 인터페이스 구현체에 위임한다.


- `UserDetailsService` 인터페이스 구현체
    - `org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl` 구현체를 사용하면 된다.

> ### JdbcDaoImpl

- JDBC를 통해 데이터베이스에서 사용자 인증 정보를 가져온다.
- `JdbcDaoImpl` 객체를 Bean으로 등록하면 데이터베이스 기반 인증 처리가 완료된다.
- `jdbcAuthentication` 메서드는 `UserDetailsService` 인터페이스 구현체로 `JdbcUserDetailsManager` 객체를 등록한다.


- `JdbcUserDetailsManager` 클래스는 `JdbcDaoImpl` 클래스를 상속한다.

# 데이터베이스 기반 인증

- `UserDetailsService` 커스텀 구현체를 만들지 않아도 된다.
- `JdbcDaoImpl` 클래스를 설정하면 된다.


> ### JdbcDaoImpl

- 기존 데이터베이스 스키마에 적용할 수 있다.
- 기존 기능을 더욱 정교하게 설정할 수 있도록 다양한 옵션을 제공한다.

```sql
CREATE TABLE permissions
(
    id   bigint      NOT NULL,
    name varchar(20) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE groups
(
    id   bigint      NOT NULL,
    name varchar(20) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE group_permission
(
    id            bigint NOT NULL,
    group_id      bigint NOT NULL,
    permission_id bigint NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT unq_group_id_permission_id UNIQUE (group_id, permission_id),
    CONSTRAINT fk_group_id_for_group_permission FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_permission_id_for_group_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE users
(
    id       bigint      NOT NULL,
    login_id varchar(20) NOT NULL,
    passwd   varchar(80) NOT NULL,
    group_id bigint      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT unq_login_id UNIQUE (login_id),
    CONSTRAINT fk_group_id_for_user FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
```


- Group-based Access Control
    - 사용자와 권한 사이에 그룹이나를 간접 계층을 둘 수 있다.
    - 사용자는 특정 그룹에 속하게 된다.
    - 그룹은 권한 집합을 참조한다.
        - 사용자를 특정 그룹에 속하게 하여 그룹에 속한 권한을 일괄 적용할 수 있다.


> ### JdbcDaoImpl 클래스 재정의
 
- 수행 목적에 따라 테이블 구조에 맞게 재정의하여 활용한다.
  - userByUsernameQuery - 기본 쿼리

```sql
select username, authority
from authorities
where username = ?
```

- authoritiesByUsernameQuery - 기본 쿼리
  - 사용자에게 직접 부여된 하나 이상의 권한을 반환한다.

```sql
select username, authority
from authorities
where username = ?
```

- groupAuthoritiesByUsernameQuery
  - 그룹 멤버십을 통해 사용자에게 승인된 권한을 반환한다.

```sql
select g.id, g.group_name, ga.authority
from groups g,
     group_members gm,
     group_authorities ga
where gm.username = ?
  and g.id = ga.group_id
  and g.id = gm.group_id
```

- JdbcDaoImpl 객체를 Bean으로 등록
  - enableGroups
    - Group-based Access Control 활용시 : true
    - groupAuthoritiesByUsername 쿼리 정의시 자동으로 true 설정
  - enableAuthorities
    - Group-based Access Control 활용시 : false

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