package com.example.hyena.configures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfigure extends WebSecurityConfigurerAdapter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

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

    public WebSecurityConfigure() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/assets/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/me").hasAnyRole("USER", "ADMIN")
                .antMatchers("/admin").access("hasRole('Admin') and isFullyAuthenticated()")
                .anyRequest().permitAll()
                .and()
                .formLogin()
                .defaultSuccessUrl("/")

                // front component id 이름
                .usernameParameter("my-username")
                .passwordParameter("my-pass")
                .permitAll()
                .and()

                .httpBasic()
                .and()

                // 로그아웃 기능
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // default
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true) // default
                .clearAuthentication(true) // default
                .and()

                // 자동 로그인
                .rememberMe()
                .rememberMeParameter("remember-me")
                .tokenValiditySeconds(300)
                .and()

                /**
                 * HTTP 요청을 HTTPS 요청으로 리다이렉트 한다.
                 */
                .requiresChannel()
                // 모두 https가 필요하다.
                .anyRequest().requiresSecure()
                .and()

                /**
                 * 세션 관리
                 */
                .sessionManagement()
                .sessionFixation().changeSessionId()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/")
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .and()
                .and()


                /**
                 * 익명 권한을 커스텀한다.
                 */
                .anonymous()
                .principal("thisIsAnonymousUser")
                .authorities("ROLE_ANONYMOUS", "ROLE_UNKNOWN")

                /**
                 * AccessDeniedHandler 추가
                 */
                .and()
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("user").password("{noop}user123").roles("USER")
                .and()
                .withUser("admin01").password("{noop}admin123").roles("ADMIN")
                .and()
                .withUser("admin02").password("{noop}admin123").roles("ADMIN");
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, e) -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication != null ? authentication.getPrincipal() : null;
            logger.warn("{} is denied", principal, e);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("text/plain");
            response.getWriter().write("## ACCESS DENIED !! ##");
            response.getWriter().flush();
            response.getWriter().close();
        };
    }
}
