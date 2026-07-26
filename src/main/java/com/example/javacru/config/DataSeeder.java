package com.example.javacru.config;

import com.example.javacru.model.Product;
import com.example.javacru.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedProducts(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() > 0) {
                return;
            }

            Product laptop = new Product();
            laptop.setName("Laptop Pro 14");
            laptop.setDescription("Lightweight laptop for development work");
            laptop.setPrice(new BigDecimal("1299.00"));
            laptop.setStock(15);

            Product mouse = new Product();
            mouse.setName("Wireless Mouse");
            mouse.setDescription("Ergonomic wireless mouse");
            mouse.setPrice(new BigDecimal("39.99"));
            mouse.setStock(120);

            Product keyboard = new Product();
            keyboard.setName("Mechanical Keyboard");
            keyboard.setDescription("RGB mechanical keyboard with hot-swap switches");
            keyboard.setPrice(new BigDecimal("149.50"));
            keyboard.setStock(45);

            productRepository.save(laptop);
            productRepository.save(mouse);
            productRepository.save(keyboard);
        };
    }
}
