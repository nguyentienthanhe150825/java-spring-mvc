package vn.hoidanit.laptopshop.controller.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;

import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetail;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.service.ProductService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class ItemController {

    // DI
    private final ProductService productService;

    public ItemController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/product/{id}")
    public String getProductPage(Model model, @PathVariable long id) {
        List<Product> listProduct = this.productService.getList();
        Product product = this.productService.getProductById(id).get();
        List<Product> productFactory = this.productService.getProductByFactory(product.getFactory());
        model.addAttribute("productFactory", productFactory);
        model.addAttribute("productFactorySize", productFactory.size());
        model.addAttribute("listProduct", listProduct);
        model.addAttribute("product", product);
        model.addAttribute("id", id);
        return "client/product/detail";
    }

    @PostMapping("/add-product-to-cart/{id}")
    public String addProductToCart(@PathVariable long id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        long productId = id;
        String email = (String) session.getAttribute("email");

        this.productService.handleAddProductToCart(email, productId, session, 1);

        return "redirect:/";
    }

    @GetMapping("/cart")
    public String getCartPage(Model model, HttpServletRequest request) {
        User currentUser = new User();
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        Cart cart = this.productService.fetchByUser(currentUser);

        List<CartDetail> cartDetails = cart == null ? new ArrayList<CartDetail>() : cart.getCartDetails();

        double totalPrice = 0;
        for (CartDetail cd : cartDetails) {
            totalPrice += cd.getPrice() * cd.getQuantity();
        }

        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("totalPrice", totalPrice);

        model.addAttribute("cart", cart);

        return "client/cart/show";
    }

    @PostMapping("/delete-cart-product/{id}")
    public String deleteCartDetail(@PathVariable long id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        long cartDetailId = id;
        this.productService.handleRemoveCartDetail(cartDetailId, session);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String getCheckOutPage(Model model, HttpServletRequest request) {
        User currentUser = new User();// null
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        Cart cart = this.productService.fetchByUser(currentUser);

        List<CartDetail> cartDetails = cart == null ? new ArrayList<CartDetail>() : cart.getCartDetails();

        double totalPrice = 0;
        for (CartDetail cd : cartDetails) {
            totalPrice += cd.getPrice() * cd.getQuantity();
        }

        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("totalPrice", totalPrice);

        return "client/cart/checkout";

    }

    @PostMapping("/confirm-checkout")
    public String getCheckoutPage(@ModelAttribute("cart") Cart cart) {
        List<CartDetail> cartDetails = cart == null ? new ArrayList<>() : cart.getCartDetails();
        this.productService.handleUpdateCartBeforeCheckout(cartDetails);
        return "redirect:/checkout";
    }

    @PostMapping("/place-order")
    public String handlePlaceOrder(
            HttpServletRequest request,
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverAddress") String receiverAddress,
            @RequestParam("receiverPhone") String receiverPhone) {

        HttpSession session = request.getSession(false);
        User currentUser = new User();
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        this.productService.handlePlaceOrder(currentUser, session, receiverName, receiverAddress, receiverPhone);

        return "redirect:/thanks";
    }

    @GetMapping("/thanks")
    public String getThankYouPage() {
        return "client/cart/orderSuccess";
    }

    @PostMapping("/add-product-from-view-detail")
    public String handleAddProductFromViewDetail(@RequestParam("id") long id, @RequestParam("quantity") long quantity,
            HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String email = (String) session.getAttribute("email");

        this.productService.handleAddProductToCart(email, id, session, quantity);
        return "redirect:/product/" + id;
    }

    @GetMapping("/products")
    public String getProductPage(Model model,
            @RequestParam("page") Optional<String> pageOptional,
            @RequestParam("name") Optional<String> nameOptional,
            @RequestParam("min-price") Optional<String> minOptional,
            @RequestParam("max-price") Optional<String> maxOptional,
            @RequestParam("factory") Optional<String> factoryOptional,
            @RequestParam("price") Optional<String> priceOptional,
            @RequestParam("target") Optional<String> targetOptional,
            @RequestParam("sort") Optional<String> sortOptional) {

        int page = 1;
        try {
            if (pageOptional.isPresent()) {
                // Convert from String to int
                page = Integer.parseInt(pageOptional.get());
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        Pageable pageable = PageRequest.of(page - 1, 10);

        String name = nameOptional.isPresent() ? nameOptional.get() : "";
        Page<Product> productsPage = this.productService.getAllProductsWithSpec(pageable, name);

        // case 1:Lấy ra tất cả sản phẩm có giá cả tối thiểu là 1000 (vnd)
        // double min = minOptional.isPresent() ? Double.parseDouble(minOptional.get()) : 0;
        // Page<Product> productsPage = this.productService.getAllProductsWithSpecMin(pageable, min);

        // case 2: Lấy ra tất cả sản phẩm có giá cả tối đa là 100000 (vnd)
        // double max = maxOptional.isPresent() ? Double.parseDouble(maxOptional.get()) : 0;
        // Page<Product> productsPage = this.productService.getAllProductsWithSpecMax(pageable, max);

        // case 3: Lấy ra tất cả sản phẩm có hãng sản xuất = APPLE
        // String factory = factoryOptional.isPresent() ? factoryOptional.get() : "";
        // Page<Product> productsPage = this.productService.getAllProductsWithSpecFactory(pageable, factory);

        // case 4: Lấy ra tất cả sản phẩm có hãng sản xuất = APPLE hoặc DELL . Truyền
        // nhiều điều kiện, ngăn cách các giá trị bởi dấu phẩy (điều kiện IN)
        // List<String> listFactory = Arrays.asList(factoryOptional.get().split(","));
        // Page<Product> productsPage = this.productService.getAllProductsWithSpecListFactory(pageable, listFactory);

        // case 5: Lấy ra tất cả sản phẩm theo range (khoảng giá). 10 triệu < price <= 15 triệu
        // String price = priceOptional.isPresent() ? priceOptional.get() : "";
        // Page<Product> productsPage = this.productService.getAllProductsWithSpecPrice(pageable, price);

        // case 6: Lấy ra tất cả sản phẩm theo range (khoảng giá). 10 triệu < price <= 15 triệu và 15 triệu < price <= 30
        // List<String> listPrice = Arrays.asList(priceOptional.get().split(","));
        // Page<Product> productsPage = this.productService.getAllProductsWithSpecListPrice(pageable, listPrice);

        List<Product> products = productsPage.getContent();

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productsPage.getTotalPages());

        return "client/product/show";
    }

}
