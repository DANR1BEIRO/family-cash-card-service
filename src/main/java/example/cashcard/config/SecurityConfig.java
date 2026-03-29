package example.cashcard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * The {@code @Configuration} annotation tells Spring to use this class to configure Spring and Spring Boot itself.
 * Any Beans specified in this class will now be available to Spring's Auto Configuration engine.
 */
@Configuration
class SecurityConfig {

    /**
     * Spring Security expects a Bean to configure its Filter Chain.
     * Annotating a method returning a {@link SecurityFilterChain} with the {@code @Bean} satisfies this expectation.
     *
     * @param http O objeto {@link HttpSecurity} usado para montar as regras de segurança.
     * @return O {@link SecurityFilterChain} que será injetado no contexto do Spring.
     * @throws Exception Se houver erro nas definições de segurança ou no build do objeto.
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        /**
         * All HTTP requests to cashcards/ endpoints are required to be authenticated
         * using HTTP Basic Authentication security (username and password).
         */
        http.authorizeHttpRequests(request -> request
                        .requestMatchers("/cashcards/**")
                        .authenticated())
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring's IoC container will find the {@link UserDetailsService}
     * Bean and Spring Data will use it when needed.
     * @param passwordEncoder
     * @return
     */
    @Bean
    UserDetailsService testOnlyUsers(PasswordEncoder passwordEncoder) {
        User.UserBuilder users = User.builder();
        UserDetails goku = users
                .username("goku")
                .password(passwordEncoder.encode("123"))
                .roles()
                .build();
        return new InMemoryUserDetailsManager(goku);
    }
}
