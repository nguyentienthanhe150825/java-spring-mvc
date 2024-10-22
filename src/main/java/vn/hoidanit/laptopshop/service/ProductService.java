package vn.hoidanit.laptopshop.service;

import java.util.List;

import org.springframework.stereotype.Service;

import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.repository.ProductRepository;

@Service
public class ProductService {

    //DI
    private final ProductRepository productRepository;

    public ProductService (ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct (Product product) {
        Product saveProduct = this.productRepository.save(product);
        return saveProduct;
    }
    
    public List<Product> getAllProducts () {
        return this.productRepository.findAll();
    }
}
