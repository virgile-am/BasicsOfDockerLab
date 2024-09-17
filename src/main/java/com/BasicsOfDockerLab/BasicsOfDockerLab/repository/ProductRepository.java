package com.BasicsOfDockerLab.BasicsOfDockerLab.repository;


import com.BasicsOfDockerLab.BasicsOfDockerLab.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
