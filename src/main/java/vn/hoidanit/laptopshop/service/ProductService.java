package vn.hoidanit.laptopshop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetail;
import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.domain.OrderDetail;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.domain.dto.ProductCriteriaDTO;
import vn.hoidanit.laptopshop.repository.CartDetailRepository;
import vn.hoidanit.laptopshop.repository.CartRepository;
import vn.hoidanit.laptopshop.repository.OrderDetailRepository;
import vn.hoidanit.laptopshop.repository.OrderRepository;
import vn.hoidanit.laptopshop.repository.ProductRepository;
import vn.hoidanit.laptopshop.service.specification.ProductSpecs;

@Service
public class ProductService {

    // DI
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    public ProductService(ProductRepository productRepository, CartRepository cartRepository,
            CartDetailRepository cartDetailRepository, UserService userService, OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    public Product createProduct(Product product) {
        Product saveProduct = this.productRepository.save(product);
        return saveProduct;
    }

    public List<Product> getList() {
        return this.productRepository.findAll();
    }

    // Pagination and Filter
    public Page<Product> getAllProductsWithProductCriteriaDTO(Pageable page, ProductCriteriaDTO productCriteriaDTO) {
        // Lần đầu tiên vào page
        if (productCriteriaDTO.getTarget() == null && productCriteriaDTO.getFactory() == null
                && productCriteriaDTO.getPrice() == null) {
            return this.productRepository.findAll(page);
        }

        // Filter Target
        Specification<Product> combinedSpec = Specification.where(null);
        if (productCriteriaDTO.getTarget() != null && productCriteriaDTO.getTarget().isPresent()) {
            Specification<Product> currentSpecs = ProductSpecs.matchListTarget(productCriteriaDTO.getTarget().get());
            combinedSpec = combinedSpec.and(currentSpecs);
        }

        // Filter Factory
        if (productCriteriaDTO.getFactory() != null && productCriteriaDTO.getFactory().isPresent()) {
            Specification<Product> currentSpecs = ProductSpecs.matchListFactory(productCriteriaDTO.getFactory().get());
            combinedSpec = combinedSpec.and(currentSpecs);
        }

        // Filter Price
        if (productCriteriaDTO.getPrice() != null && productCriteriaDTO.getPrice().isPresent()) {
            Specification<Product> currentSpecs = this.buildPriceSpecification(productCriteriaDTO.getPrice().get());
            combinedSpec = combinedSpec.and(currentSpecs);
        }

        
        return this.productRepository.findAll(combinedSpec, page);
    }

    public Specification<Product> buildPriceSpecification(List<String> price) {
        Specification<Product> combinedSpec = (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
        for (String p : price) {
            double min = 0;
            double max = 0;
            // Set the appropriate min and max based on the price range string
            switch (p) {
                case "duoi-10-trieu":
                    min = 0;
                    max = 10000000;
                    break;
                case "10-15-trieu":
                    min = 10000000;
                    max = 15000000;
                    break;
                case "15-20-trieu":
                    min = 15000000;
                    max = 20000000;
                    break;
                case "tren-20-trieu":
                    min = 20000000;
                    max = 200000000;
                    break;
            }
            if (min != 0 && max != 0) {
                Specification<Product> rangeSpec = ProductSpecs.matchMultiplePrice(min, max);
                combinedSpec = combinedSpec.or(rangeSpec);
            }
        }
        return combinedSpec;
    }

    // Pagination and Filter
    public Page<Product> getAllProductsWithSpec(Pageable page, String name) {
        return this.productRepository.findAll(ProductSpecs.nameLike(name), page);
    }

    // case 1:
    public Page<Product> getAllProductsWithSpecMin(Pageable page, double min) {
        return this.productRepository.findAll(ProductSpecs.minPrice(min), page);
    }

    // case 2:
    public Page<Product> getAllProductsWithSpecMax(Pageable page, double max) {
        return this.productRepository.findAll(ProductSpecs.maxPrice(max), page);
    }

    // case 3:
    public Page<Product> getAllProductsWithSpecFactory(Pageable page, String factory) {
        return this.productRepository.findAll(ProductSpecs.matchFactory(factory), page);
    }

    // case 4:
    public Page<Product> getAllProductsWithSpecListFactory(Pageable page, List<String> factory) {
        return this.productRepository.findAll(ProductSpecs.matchListFactory(factory), page);
    }

    // case 5:
    public Page<Product> getAllProductsWithSpecPrice(Pageable page, String price) {
        // eg: price: 10-toi-15-trieu
        if (price.equals("10-toi-15-trieu")) {
            double min = 10000000;
            double max = 15000000;
            return this.productRepository.findAll(ProductSpecs.matchPrice(min, max), page);
        } else if (price.equals("15-toi-30-trieu")) {
            double min = 15000000;
            double max = 30000000;
            return this.productRepository.findAll(ProductSpecs.matchPrice(min, max), page);
        } else {
            return this.productRepository.findAll(page);
        }
    }

    // case 6:
    public Page<Product> getAllProductsWithSpecListPrice(Pageable page, List<String> price) {
        // Trong lần chạy đầu tiên thì combinedSpec = null
        // Sử dụng disjunction() để combinedSpec khác null
        Specification<Product> combinedSpec = (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
        int count = 0;
        for (String p : price) {
            double min = 0;
            double max = 0;

            // Set the appropriate min and max based on the price range string
            switch (p) {
                case "10-toi-15-trieu":
                    min = 10000000;
                    max = 15000000;
                    count++;
                    break;
                case "15-toi-20-trieu":
                    min = 15000000;
                    max = 20000000;
                    count++;
                    break;
                case "20-toi-30-trieu":
                    min = 20000000;
                    max = 30000000;
                    count++;
                    break;
            }

            if (min != 0 && max != 0) {
                Specification<Product> rangeSpec = ProductSpecs.matchMultiplePrice(min, max);
                combinedSpec = combinedSpec.or(rangeSpec);
            }
        }

        // Check if any price ranges were added (combinedSpec is empty)
        if (count == 0) {
            return this.productRepository.findAll(page);
        }

        return this.productRepository.findAll(combinedSpec, page);
    }

    // Pagination
    public Page<Product> getAllProducts(Pageable page) {
        return this.productRepository.findAll(page);
    }

    public List<Product> getProductByFactory(String factory) {
        return this.productRepository.findByFactory(factory);
    }

    public Optional<Product> getProductById(long id) {
        return this.productRepository.findById(id);
    }

    public void deleteProduct(long id) {
        this.productRepository.deleteById(id);
    }

    public void handleAddProductToCart(String email, long productId, HttpSession session, long quantity) {
        // check user đã có cart chưa ? nếu chưa -> tạo mới
        User user = this.userService.getUserByEmail(email);
        if (user != null) {
            Cart cart = this.cartRepository.findByUser(user);

            // Nếu chưa có giỏ hàng
            if (cart == null) {
                // tạo mới cart
                Cart createCart = new Cart();
                createCart.setUser(user);
                createCart.setSum(0);

                cart = this.cartRepository.save(createCart);
            }

            // save cart_detail
            // Find Product by id
            Optional<Product> productOptional = this.productRepository.findById(productId);
            if (productOptional.isPresent()) {
                Product realProduct = productOptional.get();

                // check sản phẩm đã từng được thêm vào giỏ hàng trước đây chưa?
                // Nếu đã từng thêm từ trước đó thì quantity += 1
                boolean isExistProductInCart = this.cartDetailRepository.existsByCartAndProduct(cart, realProduct);

                CartDetail oldDetail = this.cartDetailRepository.findByCartAndProduct(cart, realProduct);

                if (oldDetail == null) {
                    //
                    CartDetail cartDetail = new CartDetail();
                    cartDetail.setCart(cart);
                    cartDetail.setProduct(realProduct);
                    cartDetail.setPrice(realProduct.getPrice());
                    cartDetail.setQuantity(quantity);

                    this.cartDetailRepository.save(cartDetail);

                    // update cart (sum)
                    int sum = cart.getSum() + 1;
                    cart.setSum(sum);
                    this.cartRepository.save(cart);

                    // update sum trong session để lấy ra sum rồi hiển thị ở mục Cart <header>
                    session.setAttribute("sum", sum);
                } else {
                    oldDetail.setQuantity(oldDetail.getQuantity() + quantity);
                    this.cartDetailRepository.save(oldDetail);

                }
            }
        }
    }

    public Cart fetchByUser(User user) {
        return this.cartRepository.findByUser(user);
    }

    public void handleRemoveCartDetail(long cartDetailId, HttpSession session) {
        // Step 1: Tìm cart-detail theo id
        Optional<CartDetail> cartDetaiOptional = this.cartDetailRepository.findById(cartDetailId);
        if (cartDetaiOptional.isPresent()) {
            CartDetail cartDetail = cartDetaiOptional.get();

            // Step 2: Tìm cart theo cart-detail
            Cart currentCart = cartDetail.getCart();

            // Step 3: Xóa cart-detail trong database
            this.cartDetailRepository.deleteById(cartDetailId);

            // Step 4: Update Cart
            // Step 4.1: Nếu Cart có sum > 1 => Giảm sum 1 đơn vị
            if (currentCart.getSum() > 1) {
                int sum = currentCart.getSum() - 1;
                currentCart.setSum(sum);
                // Step 4.1.1: Update sum trong session để lấy ra sum rồi hiển thị ở mục Cart
                // <header>
                session.setAttribute("sum", sum);

                // Step 4.1.2: Update Cart Table trong database
                this.cartRepository.save(currentCart);
            }
            // Step 4.2: Nếu Cart có sum = 1 => Xóa Cart và set session = 0
            else {
                // Step 4.2.1: Delete Cart Table trong database
                this.cartRepository.deleteById(currentCart.getId());

                // Step 4.2.2: Set Sum = 0
                session.setAttribute("sum", 0);
            }
        }
    }

    public void handleUpdateCartBeforeCheckout(List<CartDetail> cartDetails) {
        for (CartDetail cartDetail : cartDetails) {
            Optional<CartDetail> cdOptional = this.cartDetailRepository.findById(cartDetail.getId());
            if (cdOptional.isPresent()) {
                CartDetail currentCartDetail = cdOptional.get();
                currentCartDetail.setQuantity(cartDetail.getQuantity());
                this.cartDetailRepository.save(currentCartDetail);
            }
        }
    }

    public void handlePlaceOrder(User user, HttpSession session, String receiverName, String receiverAddress,
            String receiverPhone) {

        // Step 1: get Cart by User
        Cart cart = this.cartRepository.findByUser(user);
        if (cart != null) {
            List<CartDetail> cartDetails = cart.getCartDetails();

            if (cartDetails != null) {

                // Step 2: create Order
                Order order = new Order(); // order ko có id (id = null)
                order.setUser(user);
                order.setReceiverName(receiverName);
                order.setReceiverAddress(receiverAddress);
                order.setReceiverPhone(receiverPhone);
                order.setStatus("PENDING");

                double sum = 0;
                for (CartDetail cd : cartDetails) {
                    sum += cd.getPrice() * cd.getQuantity();
                }
                order.setTotalPrice(sum);
                order = this.orderRepository.save(order); // order lúc này sẽ có id

                // Step 3: create order-detail
                for (CartDetail cd : cartDetails) {
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrder(order);
                    orderDetail.setProduct(cd.getProduct());
                    orderDetail.setPrice(cd.getPrice());
                    orderDetail.setQuantity(cd.getQuantity());

                    this.orderDetailRepository.save(orderDetail);
                }

                // Step 2: delete cart-detail
                for (CartDetail cd : cartDetails) {
                    this.cartDetailRepository.deleteById(cd.getId());

                }
                // Step 3: delete cart
                this.cartRepository.deleteById(cart.getId());

                // Step 4: update session
                session.setAttribute("sum", 0);
            }
        }
    }

    public long countProduct() {
        return this.productRepository.count();
    }
}
