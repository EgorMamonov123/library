package lab.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Разрешить доступ к Swagger UI и документации
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/api-docs/**"
                        ).permitAll()

                        // Разрешить доступ к книгам всем
                        .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll()

                        // Разрешить доступ к API для тестирования
                        .requestMatchers("/api/books/search").permitAll()
                        .requestMatchers("/api/books/available").permitAll()

                        // Требовать аутентификацию для остальных endpoints
                        .requestMatchers(HttpMethod.POST, "/api/books/**").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.PUT, "/api/books/**").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")

                        .requestMatchers("/api/readers/**").hasRole("LIBRARIAN")
                        .requestMatchers("/api/loans/**").hasRole("LIBRARIAN")
                        .requestMatchers("/api/my-loans/**").authenticated()

                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults()); // Добавить форму логина

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails reader = User.withUsername("reader")
                .password(passwordEncoder().encode("password"))
                .roles("READER")
                .build();

        UserDetails librarian = User.withUsername("librarian")
                .password(passwordEncoder().encode("librarian"))
                .roles("LIBRARIAN")
                .build();

        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder().encode("admin"))
                .roles("ADMIN", "LIBRARIAN") // Админ тоже может быть библиотекарем
                .build();

        return new InMemoryUserDetailsManager(reader, librarian, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}