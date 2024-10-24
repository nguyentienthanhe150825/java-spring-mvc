package vn.hoidanit.laptopshop.controller.client;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;

import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.service.ProductService;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ItemController {

    //DI
    private final ProductService productService;

    public ItemController (ProductService productService) {
        this.productService = productService;
    }
    
    @GetMapping("/product/{id}")
    public String getProductPage(Model model, @PathVariable long id) {
        List<Product> listProduct = this.productService.getAllProducts();
        Product product = this.productService.getProductById(id).get();
        List<Product> productFactory = this.productService.getProductByFactory(product.getFactory());
        model.addAttribute("productFactory", productFactory);
        model.addAttribute("productFactorySize", productFactory.size());
        model.addAttribute("listProduct", listProduct);
        model.addAttribute("product", product);
        model.addAttribute("id", id);
        return "client/product/detail";
    }
}
