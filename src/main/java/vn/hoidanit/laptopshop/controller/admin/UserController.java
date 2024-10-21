package vn.hoidanit.laptopshop.controller.admin;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import vn.hoidanit.laptopshop.domain.Role;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.service.UploadService;
import vn.hoidanit.laptopshop.service.UserService;

@Controller
public class UserController {

    // DI
    private final UserService userService;
    private final UploadService uploadService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, UploadService uploadService,
    PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.uploadService = uploadService;
        this.passwordEncoder = passwordEncoder;
    }

    // Mô hình MVC: video 56
    // Người dùng vào website sẽ chạy vào RequestMapping("/")
    // Sau đó Controller sẽ gọi tới model (userService)
    // Model sẽ xử lý dữ liệu và trả về controller (model.addAttribute)
    // Controller sẽ return ra file jsp

    @RequestMapping("/")
    public String getHomePage(Model model) {
        List<User> arrUsers = this.userService.getAllUsersByEmail("1@gmail.com");
        model.addAttribute("eric", "test");
        model.addAttribute("hoidanit", "from controller with model");
        return "hello";
    }

    @RequestMapping("/admin/user")
    public String getUserPage(Model model) {
        List<User> users = this.userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/user/show";
    }

    @RequestMapping("/admin/user/{id}")
    public String getUserDetailPage(Model model, @PathVariable long id) {
        User user = this.userService.getUserById(id);
        model.addAttribute("id", id);
        model.addAttribute("user", user);
        return "admin/user/detail";
    }

    @GetMapping("/admin/user/create") // GET
    public String getCreateUserPage(Model model) {
        model.addAttribute("newUser", new User());
        return "admin/user/create";
    }

    @PostMapping(value = "/admin/user/create")
    public String createUserPage(@ModelAttribute("newUser") User dataUser,
            @RequestParam("uploadFile") MultipartFile file) {

        String avatar = this.uploadService.handleSaveUploadFile(file, "avatar");

        // Link mã hóa BCrypt:
        // https://stackoverflow.com/questions/55548290/using-bcrypt-in-spring
        String hashPassword = this.passwordEncoder.encode(dataUser.getPassword());

        dataUser.setAvatar(avatar);
        dataUser.setPassword(hashPassword);

        //Dữ liệu từ front-end gửi về là role.name
        //Trong khi ở Object User thì role là 1 đối tượng (Role)
        //Do đó: đầu tiên cần phải tìm đối tượng Role theo role.name
        Role roleCreate = this.userService.getRoleByName(dataUser.getRole().getName());
        
        //Sau đó: sẽ lưu vào database dưới dạng roleId chứ ko phải roleName
        //Lưu RoleId vì ở class User thì mapping bảng Role = @JoinColumn(name = "role_id")
        dataUser.setRole(roleCreate);

        //save
        this.userService.handleSaveUser(dataUser);
        return "redirect:/admin/user";
    }

    @RequestMapping("/admin/user/update/{id}") // GET
    public String getUpdateUserPage(Model model, @PathVariable long id) {
        User currentUser = this.userService.getUserById(id);
        model.addAttribute("newUser", currentUser);
        return "admin/user/update";
    }

    @PostMapping("/admin/user/update")
    public String postUpdateUser(@ModelAttribute("newUser") User dataUser) {
        User currentUser = this.userService.getUserById(dataUser.getId());
        if (currentUser != null) {
            currentUser.setAddress(dataUser.getAddress());
            currentUser.setFullName(dataUser.getFullName());
            currentUser.setPhone(dataUser.getPhone());

            // Vì password không có trong file jsp do đó muốn giữ nguyên password mà ko bị
            // cập nhật lại là null
            // thì cần update currentUser chứ không phải dataUser vì trong dataUser thì
            // password = null
            this.userService.handleSaveUser(currentUser);
        }
        return "redirect:/admin/user";
    }

    @GetMapping("/admin/user/delete/{id}")
    public String getDeleteUserPage(Model model, @PathVariable long id) {
        User currentUser = this.userService.getUserById(id);
        model.addAttribute("id", id);
        model.addAttribute("newUser", currentUser);
        return "admin/user/delete";
    }

    @PostMapping("/admin/user/delete")
    public String postDeleteUser(@ModelAttribute("newUser") User dataUser) {
        this.userService.deleteUser(dataUser.getId());
        return "redirect:/admin/user";
    }
}
