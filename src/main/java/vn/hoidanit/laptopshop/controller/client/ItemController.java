package vn.hoidanit.laptopshop.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ItemController {
    
    @GetMapping("/product/{id}")
    public String getProductPage(Model model, @PathVariable long id) {
        return "client/product/detail";
    }
}
