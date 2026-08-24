package com.example.javacru.controller;

import com.example.javacru.exception.ResourceNotFoundException;
import com.example.javacru.model.Product;
import com.example.javacru.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private Product sampleProduct(Long id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription("A sample product");
        product.setPrice(new BigDecimal("19.99"));
        product.setStock(10);
        return product;
    }

    @Test
    void getAll_returnsListOfProducts() throws Exception {
        when(productService.findAll()).thenReturn(List.of(sampleProduct(1L, "Laptop")));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    void getById_returnsProduct_whenFound() throws Exception {
        when(productService.findById(1L)).thenReturn(sampleProduct(1L, "Laptop"));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void getById_returns404_whenNotFound() throws Exception {
        when(productService.findById(99L)).thenThrow(new ResourceNotFoundException("Product not found with id: 99"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void create_returnsCreated_withValidProduct() throws Exception {
        Product input = sampleProduct(null, "Keyboard");
        Product saved = sampleProduct(1L, "Keyboard");
        when(productService.create(any(Product.class))).thenReturn(saved);

        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Keyboard"));
    }

    @Test
    void create_returns400_whenNameBlank() throws Exception {
        Product input = sampleProduct(null, "");

        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_returnsUpdatedProduct() throws Exception {
        Product updates = sampleProduct(null, "Laptop Pro");
        Product result = sampleProduct(1L, "Laptop Pro");
        when(productService.update(eq(1L), any(Product.class))).thenReturn(result);

        mockMvc.perform(put("/api/products/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop Pro"));
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService).delete(1L);
    }

    @Test
    void delete_returns404_whenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Product not found with id: 99"))
                .when(productService).delete(anyLong());

        mockMvc.perform(delete("/api/products/99"))
                .andExpect(status().isNotFound());
    }
}
