package vn.hoidanit.laptopshop.service.specification;

import org.springframework.data.jpa.domain.Specification;

import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.Product_;

public class ProductSpecs {

    // Sử dụng static để thông qua class gọi trực tiếp tới đối tượng hoặc phương
    // thức mà ko cần phải tạo mới đối tượng

    public static Specification<Product> nameLike(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get(Product_.NAME), "%" + name + "%");
    }
}
