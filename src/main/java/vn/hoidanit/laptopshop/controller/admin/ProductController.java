package vn.hoidanit.laptopshop.controller.admin;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.service.ProductService;
import vn.hoidanit.laptopshop.service.UploadService;

@Controller
public class ProductController {

    // DI
    private final ProductService productService;
    private final UploadService uploadService;

    public ProductController(ProductService productService, UploadService uploadService) {
        this.uploadService = uploadService;
        this.productService = productService;
    }

    @GetMapping("/admin/product")
    public String getProduct(Model model) {
        List<Product> products = this.productService.getAllProducts();
        model.addAttribute("products", products);
        return "admin/product/show";
    }

    @GetMapping("/admin/product/{id}")
    public String getProductDetailPage(Model model, @PathVariable long id) {
        Product product = this.productService.getProductById(id).get();
        model.addAttribute("product", product);
        model.addAttribute("id", id);
        return "admin/product/detail";
    }

    @GetMapping("/admin/product/create")
    public String getCreateProductPage(Model model) {
        model.addAttribute("newProduct", new Product());
        return "admin/product/create";
    }

    @PostMapping("/admin/product/create")
    public String handleCreateProduct(@ModelAttribute("newProduct") @Valid Product dataProduct,
            BindingResult bindingResult,
            @RequestParam("uploadFile") MultipartFile file) {

        // Validate
        List<FieldError> errors = bindingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println(">>>>>>>>" + error.getField() + " - " + error.getDefaultMessage());
        }

        // Check valid Front-end:
        // https://mkyong.com/spring-mvc/spring-mvc-form-check-if-a-field-has-an-error/
        if (bindingResult.hasErrors()) {
            return "admin/product/create";
        }

        String imageProduct = this.uploadService.handleSaveUploadFile(file, "product");

        dataProduct.setImage(imageProduct);

        // save
        this.productService.createProduct(dataProduct);

        return "redirect:/admin/product";
    }

    @GetMapping("/admin/product/update/{id}")
    public String getUpdateProductPage(Model model, @PathVariable long id) {
        Product currentProduct = this.productService.getProductById(id).get();
        model.addAttribute("newProduct", currentProduct);
        return "admin/product/update";
    }

    @PostMapping("/admin/product/update")
    public String handleUpdateProduct(@ModelAttribute("newProduct") @Valid Product dataProduct,
            BindingResult bindingResult, @RequestParam("uploadFile") MultipartFile file) {

        // Check valid Front-end:
        // https://mkyong.com/spring-mvc/spring-mvc-form-check-if-a-field-has-an-error/
        if (bindingResult.hasErrors()) {
            return "admin/product/update";
        }

        Product currentProduct = this.productService.getProductById(dataProduct.getId()).get();

        if(currentProduct != null) {
            //update new image
            if(!file.isEmpty()) {
                String img = this.uploadService.handleSaveUploadFile(file, "product");
                currentProduct.setImage(img);
            }
            currentProduct.setName(dataProduct.getName());
            currentProduct.setPrice(dataProduct.getPrice());
            currentProduct.setQuantity(dataProduct.getQuantity());
            currentProduct.setDetailDesc(dataProduct.getDetailDesc());
            currentProduct.setShortDesc(dataProduct.getShortDesc());
            currentProduct.setFactory(dataProduct.getFactory());
            currentProduct.setTarget(dataProduct.getTarget());

            this.productService.createProduct(currentProduct);
        }

        return "redirect:/admin/product";
    }

    @GetMapping("/admin/product/delete/{id}")
    public String getDeleteProductPage(Model model, @PathVariable long id) {
        Product currentProduct = this.productService.getProductById(id).get();
        model.addAttribute("id", id);
        model.addAttribute("newProduct", currentProduct);
        return "admin/product/delete";
    }

    @PostMapping("/admin/product/delete")
    public String postDeleteProduct(@ModelAttribute("newProduct") Product dataProduct) {
        this.productService.deleteProduct(dataProduct.getId());
        return "redirect:/admin/product";
    }
}
