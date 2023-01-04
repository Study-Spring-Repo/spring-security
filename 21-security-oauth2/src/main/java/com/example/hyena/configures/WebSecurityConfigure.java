package com.example.hyena.configures;

import com.example.hyena.jwt.Jwt;
import com.example.hyena.jwt.JwtAuthenticationFilter;
import com.example.hyena.oauth2.OAuth2AuthenticationSuccessHandler;
import com.example.hyena.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class WebSecurityConfigure extends WebSecurityConfigurerAdapter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final JwtConfigure jwtConfigure;

    private final UserService userService;

    public WebSecurityConfigure(JwtConfigure jwtConfigure, UserService userService) {
        this.jwtConfigure = jwtConfigure;
        this.userService = userService;
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/assets/**", "/h2-console/**");
    }

    @Bean
    public Jwt jwt() {
        return new Jwt(
                jwtConfigure.getIssuer(),
                jwtConfigure.getClientSecret(),
                jwtConfigure.getExpirySeconds()
        );
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, e) -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication != null ? authentication.getPrincipal() : null;
            logger.warn("{} is denied", principal, e);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("## ACCESS DENIED !! ##");
            response.getWriter().flush();
            response.getWriter().close();
        };
    }

    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        Jwt jwt = getApplicationContext().getBean(Jwt.class);
        return new JwtAuthenticationFilter(jwtConfigure.getHeader(), jwt);
    }

    @Bean
    public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
        Jwt jwt = getApplicationContext().getBean(Jwt.class);
        return new OAuth2AuthenticationSuccessHandler(jwt, userService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/api/user/me").hasAnyRole("USER", "ADMIN")
                .anyRequest().permitAll()
                .and()
                .csrf()
                .disable()
                .headers()
                .disable()
                .formLogin()
                .disable()
                .httpBasic()
                .disable()

                // 로그아웃 기능
                .logout()
                .disable()

                // 자동 로그인
                .rememberMe()
                .disable()

                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .oauth2Login()
                .successHandler(oAuth2AuthenticationSuccessHandler())
                .and()

                /**
                 * AccessDeniedHandler 추가
                 */
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler())
                .and()

                .addFilterAfter(jwtAuthenticationFilter(), SecurityContextPersistenceFilter.class);
    }
}
