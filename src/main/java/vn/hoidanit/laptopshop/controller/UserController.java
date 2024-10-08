package vn.hoidanit.laptopshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.hoidanit.laptopshop.service.UserService;

@Controller
public class UserController {

    // DI
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //Mô hình MVC: video 56
    //Người dùng vào website sẽ chạy vào RequestMapping("/")
    //Sau đó Controller sẽ gọi tới model (userService)
    //Model sẽ xử lý dữ liệu và trả về controller (model.addAttribute)
    //Controller sex return ra file jsp 

    @RequestMapping("/")
    public String getHomePage(Model model) {
        String test = this.userService.handleHello();
        model.addAttribute("eric", test);
        model.addAttribute("hoidanit", "from controller with model");
        return "hello";
    }
}

// @RestController
// public class UserController {

// //DI
// private UserService userService;

// public UserController(UserService userService) {
// this.userService = userService;
// }

// @GetMapping("")
// public String getHomePage() {
// return this.userService.handleHello();
// }
// }
