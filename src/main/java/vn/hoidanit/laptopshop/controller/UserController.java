package vn.hoidanit.laptopshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.service.UserService;

@Controller
public class UserController {

    // DI
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //Mô hình MVC: video 56
    //Người dùng vào website sẽ chạy vào RequestMapping("/")
    //Sau đó Controller sẽ gọi tới model (userService)
    //Model sẽ xử lý dữ liệu và trả về controller (model.addAttribute)
    //Controller sẽ return ra file jsp 

    @RequestMapping("/")
    public String getHomePage(Model model) {
        model.addAttribute("eric", "test");
        model.addAttribute("hoidanit", "from controller with model");
        return "hello";
    }

    @RequestMapping("/admin/user")
    public String getUserPage(Model model) {
        model.addAttribute("newUser", new User());
        return "admin/user/create";
    }

    @RequestMapping(value = "/admin/user/create1", method = RequestMethod.POST)
    public String createUserPage(Model model, @ModelAttribute("newUser") User dataUser) {
        System.out.println("Data: " + dataUser);
        this.userService.handleSaveUser(dataUser);
        return "hello";
    }
}


