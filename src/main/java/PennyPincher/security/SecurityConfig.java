package PennyPincher.security;

import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@Data
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    public static PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,  HandlerMappingIntrospector introspector) throws Exception {
        http
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .requestMatchers(new MvcRequestMatcher(introspector, "/h2-console/**")).permitAll()
                                .requestMatchers(new MvcRequestMatcher(introspector, "/")).permitAll()
                                .requestMatchers(new MvcRequestMatcher(introspector, "/register/**")).permitAll()
                                .requestMatchers(new MvcRequestMatcher(introspector,"/login")).permitAll()
                                .anyRequest().authenticated()
                ).formLogin(
                        form -> form
                                .loginPage("/login")
                                .defaultSuccessUrl("/profile")
                                .permitAll()
                ).logout(
                        logout -> logout
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .permitAll()
                ).csrf().disable().headers().frameOptions().disable();
        return http.build();
    }

    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }
}


