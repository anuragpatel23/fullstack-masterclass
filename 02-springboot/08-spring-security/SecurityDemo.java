/**
 * TOPIC: Spring Security (Boot 3.x) — SecurityFilterChain bean, JWT filter,
 * UserDetailsService, method security, BCrypt. The modern (non-deprecated) shapes.
 */
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.*;
import java.util.List;

public class SecurityDemo {

    // ===== 1. The central config: SecurityFilterChain bean (Boot 3 style ⭐) =====
    @Configuration
    @EnableWebSecurity
    @EnableMethodSecurity                          // enables @PreAuthorize etc.
    static class SecurityConfig {

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtFilter) throws Exception {
            http
                .csrf(csrf -> csrf.disable())      // OK because: stateless, no cookie auth ⭐
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**", "/actuator/health").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated())
                // our JWT filter runs BEFORE the username/password filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
            return http.build();
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();    // adaptive, salted, deliberately slow ⭐
        }
    }

    // ===== 2. UserDetailsService: how Spring loads YOUR users =====
    @Service
    static class DbUserDetailsService implements UserDetailsService {
        // real code: inject UserRepository
        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            // var u = userRepo.findByUsername(username).orElseThrow(...);
            return User.withUsername(username)
                    .password("{bcrypt}$2a$10$...")            // stored HASH, never plaintext
                    .roles("USER")                              // becomes ROLE_USER authority
                    .build();
        }
    }

    // ===== 3. JWT filter: validate token, populate SecurityContext ⭐ =====
    @org.springframework.stereotype.Component
    static class JwtAuthFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                        FilterChain chain) throws java.io.IOException, jakarta.servlet.ServletException {
            String header = req.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                if (isValid(token)) {                          // verify signature + exp + issuer
                    var auth = new UsernamePasswordAuthenticationToken(
                            subjectOf(token), null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + roleOf(token))));
                    // ⭐ THE line: current request's thread now "is" this user
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
            chain.doFilter(req, res);   // continue the chain either way (401/403 decided later)
        }

        // In real code use jjwt / nimbus-jose: verify HMAC/RSA signature, exp, iss, aud.
        private boolean isValid(String token) { return token.split("\\.").length == 3; }
        private String subjectOf(String token) { return "user-from-token"; }
        private String roleOf(String token)    { return "USER"; }
    }

    // ===== 4. Method-level security with SpEL ⭐ =====
    @Service
    static class AccountService {

        @PreAuthorize("hasRole('ADMIN')")
        public void closeAnyAccount(String accountId) { /* admin-only */ }

        /** Ownership check: user can only read THEIR account. */
        @PreAuthorize("#username == authentication.name")
        public Object myAccount(String username) { return null; }

        /** Reading the current user anywhere in the call stack. */
        public String whoAmI() {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        }
    }

    /*
     * FLOW TO NARRATE:
     * 1. Request with "Authorization: Bearer x.y.z"
     * 2. JwtAuthFilter validates signature/claims -> sets Authentication in SecurityContextHolder
     * 3. AuthorizationFilter checks rules (hasRole etc.) -> 403 if insufficient, 401 if anonymous
     * 4. Controller runs; @PreAuthorize adds method-level gates via AOP proxy
     * 5. Response; context cleared (ThreadLocal) at request end
     *
     * LOGIN endpoint issues the token: verify credentials via AuthenticationManager
     * (DaoAuthenticationProvider -> UserDetailsService + BCrypt.matches) -> sign JWT -> return.
     */
}
