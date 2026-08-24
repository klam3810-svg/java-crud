package com.example.javacru.service;

import com.example.javacru.exception.ResourceNotFoundException;
import com.example.javacru.model.Product;
import com.example.javacru.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);
    }

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
    void findAll_returnsAllProductsFromRepository() {
        List<Product> products = List.of(sampleProduct(1L, "Laptop"), sampleProduct(2L, "Mouse"));
        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productService.findAll();

        assertThat(result).hasSize(2).containsExactlyElementsOf(products);
    }

    @Test
    void findById_returnsProduct_whenItExists() {
        Product product = sampleProduct(1L, "Laptop");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.findById(1L);

        assertThat(result).isEqualTo(product);
    }

    @Test
    void findById_throwsResourceNotFoundException_whenMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_setsTimestampsAndClearsId_thenSaves() {
        Product input = sampleProduct(42L, "Keyboard");
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.create(input);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();

        assertThat(saved.getId()).isNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(result).isSameAs(saved);
    }

    @Test
    void update_appliesChangesToExistingProduct_andSaves() {
        Product existing = sampleProduct(1L, "Laptop");
        Product updates = sampleProduct(null, "Laptop Pro");
        updates.setPrice(new BigDecimal("999.00"));
        updates.setStock(5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.update(1L, updates);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Laptop Pro");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("999.00"));
        assertThat(result.getStock()).isEqualTo(5);
        verify(productRepository).save(existing);
    }

    @Test
    void update_throwsResourceNotFoundException_whenMissing() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(5L, sampleProduct(null, "X")))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void delete_removesProduct_whenItExists() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.delete(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_throwsResourceNotFoundException_whenMissing() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).deleteById(anyLong());
    }
}
