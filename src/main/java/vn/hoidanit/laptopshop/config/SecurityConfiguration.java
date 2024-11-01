package vn.hoidanit.laptopshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.DispatcherType;
import vn.hoidanit.laptopshop.service.CustomUserDetailsService;
import vn.hoidanit.laptopshop.service.UserService;

@Configuration
@EnableMethodSecurity(securedEnabled = true) // https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return new CustomUserDetailsService(userService);
    }

    // https://stackoverflow.com/questions/43007763/spring-security-encoded-password-gives-me-bad-credentials
    @Bean
    public DaoAuthenticationProvider authProvider(
            PasswordEncoder passwordEncoder,
            UserDetailsService userDetailsService) {

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        // authProvider.setHideUserNotFoundExceptions(false); //Khi đăng nhập sai email
        // hoặc password sẽ báo lỗi ở CustomUserDetailsService (user not found)
        // CustomUserDetailsService

        return authProvider;
    }

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return new CustomSuccessHandler();
    }

    // Tương tự class: #SpringBootWebSecurityConfiguration
    // Thông báo với spring security để thay đổi sang form login đã được custom thay
    // cho form login mặc định ở class SpringBootWebSecurityConfiguration
    // (http.formLogin(withDefaults());)
    // ----------------------------------------
    // DispatcherType.FORWARD: Khi người dùng vào url : '/login' thì spring security
    // sẽ
    // auto forward tới trang view (cụ thể là file jsp)

    // https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html#using-authorization-expression-fields-and-methods
    // https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // v6 . lamda
        http
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.FORWARD,
                                DispatcherType.INCLUDE)
                        .permitAll()

                        // Không biết role: bất cứ ai cũng có thể truy cập vào homepage ("/") và xem chi
                        // tiết ("/product/**")
                        .requestMatchers("/", "/login", "/product/**",
                                "/client/**", "/css/**", "/js/**", "/images/**")
                        .permitAll()
                        // Default: hasRole sẽ tự động bỏ tiền tố prefix(ROLE_)
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Collections.singletonList(new
                                                                       // SimpleGrantedAuthority("ROLE_" +
                                                                       // user.getRole().getName())));
                        .anyRequest().authenticated())

                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .failureUrl("/login?error")
                        .successHandler(customSuccessHandler())
                        .permitAll());

        return http.build();
    }

}
