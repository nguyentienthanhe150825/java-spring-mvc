package vn.hoidanit.laptopshop.service;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Lấy thông in user từ database theo email
        vn.hoidanit.laptopshop.domain.User user = this.userService.getUserByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("user not found");
        }

        return new User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())));
        // Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))); // mặc
        // định là role: user
        // Default: SimpleGrantedAuthority sẽ yêu cầu thêm tiền tố prefix(ROLE_)
        // https://docs.spring.io/spring-security/reference/servlet/authorization/architecture.html#authz-authorities

    }

}

// UserDetailsService là một interface quan trọng trong Spring Security.
// Interface này định nghĩa một phương thức loadUserByUsername mà Spring Security sẽ sử
// dụng để xác thực người dùng dựa trên tên đăng nhập (username).
// Khi UserDetailsService được triển khai, nó sẽ chứa logic để lấy thông tin
// người dùng từ nguồn dữ liệu như database, sau đó chuyển đổi thành một đối
// tượng UserDetails để Spring Security có thể sử dụng.