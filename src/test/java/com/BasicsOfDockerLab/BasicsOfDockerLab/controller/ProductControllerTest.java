package com.BasicsOfDockerLab.BasicsOfDockerLab.controller;

import com.BasicsOfDockerLab.BasicsOfDockerLab.model.Product;
import com.BasicsOfDockerLab.BasicsOfDockerLab.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        // Using the all-args constructor provided by Lombok
        product1 = new Product(1L, "Product 1", "Description 1", 100.0);
        product2 = new Product(2L, "Product 2", "Description 2", 200.0);
    }

    @Test
    public void testGetAllProducts() throws Exception {
        Mockito.when(productService.getAllProducts()).thenReturn(Arrays.asList(product1, product2));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[1].name").value("Product 2"));
    }

    @Test
    public void testGetProductById_Success() throws Exception {
        Mockito.when(productService.getProductById(1L)).thenReturn(Optional.of(product1));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Product 1"));
    }

    @Test
    public void testGetProductById_NotFound() throws Exception {
        Mockito.when(productService.getProductById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateProduct() throws Exception {
        Product newProduct = new Product(3L, "Product 3", "Description 3", 300.0);
        Mockito.when(productService.createProduct(any(Product.class))).thenReturn(newProduct);

        String productJson = "{\"name\":\"Product 3\", \"price\":300.0, \"description\":\"Description 3\"}";

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Product 3"));
    }

    @Test
    public void testUpdateProduct() throws Exception {
        Mockito.when(productService.updateProduct(anyLong(), any(Product.class))).thenReturn(product1);

        String updatedProductJson = "{\"name\":\"Updated Product\", \"price\":150.0, \"description\":\"Updated Description\"}";

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedProductJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Product 1"));
    }

    @Test
    public void testDeleteProduct() throws Exception {
        Mockito.doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }
}
