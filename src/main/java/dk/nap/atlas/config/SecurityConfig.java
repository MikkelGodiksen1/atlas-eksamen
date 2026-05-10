package dk.nap.atlas.config;

import dk.nap.atlas.repository.AdminRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public UserDetailsService userDetailsService(AdminRepository adminRepository) {
        return username -> adminRepository.findByEmail(username)
            .map(a -> User.withUsername(a.getEmail())
                .password(a.getPasswordHash())
                .roles(a.getRole() == null ? "ADMIN" : a.getRole().toUpperCase())
                .build())
            .orElseThrow(() -> new UsernameNotFoundException("Admin ikke fundet"));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/designer/**", "/inquiry/**", "/contact/**",
                                 "/css/**", "/js/**", "/img/**", "/favicon.ico",
                                 "/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/login", "/error").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/admin", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .csrf(csrf -> {});
        return http.build();
    }
}
