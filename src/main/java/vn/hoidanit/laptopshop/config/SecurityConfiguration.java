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

import jakarta.servlet.DispatcherType;
import vn.hoidanit.laptopshop.service.CustomUserDetailsService;
import vn.hoidanit.laptopshop.service.UserService;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return new CustomUserDetailsService(userService);
    }

    // @Bean
    // public AuthenticationManager authenticationManager(HttpSecurity http,
    // PasswordEncoder passwordEncoder,
    // UserDetailsService userDetailsService) throws Exception {
    // AuthenticationManagerBuilder authenticationManagerBuilder = http
    // .getSharedObject(AuthenticationManagerBuilder.class);
    // authenticationManagerBuilder
    // .userDetailsService(userDetailsService)
    // .passwordEncoder(passwordEncoder);
    // return authenticationManagerBuilder.build();
    // }

    // https://stackoverflow.com/questions/43007763/spring-security-encoded-password-gives-me-bad-credentials
    @Bean
    public DaoAuthenticationProvider authProvider(
            PasswordEncoder passwordEncoder,
            UserDetailsService userDetailsService) {

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        // authProvider.setHideUserNotFoundExceptions(false); //Ném ra lỗi ở class
        // CustomUserDetailsService

        return authProvider;
    }

    // Tương tự class: #SpringBootWebSecurityConfiguration
    // Thông báo với spring security để thay đổi sang form login đã được custom thay
    // cho form login mặc định ở class SpringBootWebSecurityConfiguration
    // (http.formLogin(withDefaults());)
    // ----------------------------------------
    // DispatcherType.FORWARD: Khi người dùng vào url : '/login' thì javaspring sẽ
    // auto forward tới trang view (cụ thể là file jsp)
    // Mặc định java spring security sẽ check 2 dạng Dispatcher: FORWARD và INCLUDE
    // Nếu KO CÓ DispatcherType.INCLUDE.permitAll() thì ví dụ người dùng vào
    // request: "/"
    // => thì java spring security sẽ chặn không cho vào
    // this.productService.getAllProducts(); => project sẽ bị lỗi
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.FORWARD,
                                DispatcherType.INCLUDE)
                        .permitAll()

                        .requestMatchers("/", "/login", "/client/**", "/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated())
                // .anyRequest().permitAll()): thì tất cả request vào các url khác đều dc chấp thuận

                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .failureUrl("/login?error")
                        .permitAll());

        return http.build();
    }

}
