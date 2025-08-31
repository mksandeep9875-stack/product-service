package com.menon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("product/v1")
public class MainRestController {
    private static final Logger logger = LoggerFactory.getLogger(MainRestController.class);
    @Autowired
    ProductRepository productRepository;
    @Autowired
    TokenService tokenService;
    @Autowired
    private ApplicationContext ctx;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostMapping("create")
    public ResponseEntity<?> floatProject(@RequestBody Product product, @RequestHeader("Authorization") String token) {
        Principal principal = tokenService.validateToken(token);
        if (!"VALID".equals(principal.getState())) {
            logger.warn("Token validation failed");
            return ResponseEntity.status(401).body("Invalid token");
        }

        logger.info("Token validated successfully for user: {}", principal.getUsername());

        if (product.getVendorPhone().equals(principal.getUsername()) && principal.getType().equalsIgnoreCase("VENDOR")) // AUTHORIZATION OF REQUEST HAPPENS HERE
        {
            logger.info("Request received to create product: " + product);
            // Logic to publish project messages
            try {
                product.setId("PRODUCT-" + new Random().nextInt(1000000));
                Product savedProduct = productRepository.save(product);
                return ResponseEntity.ok(savedProduct);
            } catch (Exception e) {
                logger.error("Error saving project: " + e.getMessage());
                return ResponseEntity.status(500).body("Error saving project");
            }
        } else {
            logger.warn("Authorization failed: Vendor " + principal.getUsername() + " is not authorized to add products to catalog");
            return ResponseEntity.status(403).body("Authorization failed: You are not the owner of catalog services");
        }

    }

    @GetMapping("/active")
    public ResponseEntity<?> getAllActiveProducts(@RequestHeader("Authorization") String token) {
        Principal principal = tokenService.validateToken(token);
        if (!"VALID".equals(principal.getState())) {
            logger.warn("Token validation failed");
            return ResponseEntity.status(401).body("Invalid token");
        }
        logger.info("Token validated successfully for user: {}", principal.getUsername());
        try {
            logger.info("Fetching active products from product catalog service for user: {}", principal.getUsername());
            List<Product> activeProducts = productRepository.findByStatusIgnoreCase("AVAILABLE");
            return ResponseEntity.ok(activeProducts);
        } catch (Exception e) {
            logger.error("Error fetching active products: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error fetching active products");
        }
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @RequestBody Product updatedProduct, @RequestHeader("Authorization") String token) {
        Principal principal = tokenService.validateToken(token);
        if (!"VALID".equals(principal.getState())) {
            logger.warn("Token validation failed");
            return ResponseEntity.status(401).body("Invalid token");
        }

        logger.info("Token validated successfully for user: {}", principal.getUsername());

        if (!updatedProduct.getVendorPhone().equals(principal.getUsername()) || !"VENDOR".equalsIgnoreCase(principal.getType())) {
            logger.warn("Authorization failed: {} is not allowed to update product {}", principal.getUsername(), id);
            return ResponseEntity.status(403).body("Authorization failed: You are not authorized to update this product");
        }

        try {
            Product existingProduct = productRepository.findById(id).orElse(null);
            if (existingProduct == null) {
                logger.warn("Product with id {} not found", id);
                return ResponseEntity.status(404).body("Product not found");
            }

            setUpdatedProductDetails(updatedProduct, existingProduct);

            Product savedProduct = productRepository.save(existingProduct);

            logger.info("Product updated successfully: {}", savedProduct);
            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            logger.error("Error updating product: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error updating product");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable String id, @RequestHeader("Authorization") String token) {
        // 1️⃣ Validate token
        Principal principal = tokenService.validateToken(token);
        if (!"VALID".equals(principal.getState())) {
            logger.warn("Token validation failed");
            return ResponseEntity.status(401).body("Invalid token");
        }

        logger.info("Token validated successfully for user: {}", principal.getUsername());

        try {
            return productRepository.findById(id).map(existingProduct -> {
                // 2️⃣ Authorization: only the vendor who owns the product can delete
                if (!existingProduct.getVendorPhone().equals(principal.getUsername()) || !"VENDOR".equalsIgnoreCase(principal.getType())) {
                    logger.warn("Authorization failed: {} is not allowed to delete product {}", principal.getUsername(), id);
                    return ResponseEntity.status(403).body("Authorization failed: You are not allowed to delete this product");
                }

                // 3️⃣ Delete product
                productRepository.delete(existingProduct);
                logger.info("Product deleted successfully: {}", existingProduct);
                return ResponseEntity.ok("Product deleted successfully");
            }).orElseGet(() -> {
                logger.warn("Product with id {} not found", id);
                return ResponseEntity.status(404).body("Product not found");
            });
        } catch (Exception e) {
            logger.error("Error deleting product: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error deleting product");
        }
    }


    private static void setUpdatedProductDetails(Product updatedProduct, Product existingProduct) {
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setCategory(updatedProduct.getCategory());
        existingProduct.setStatus(updatedProduct.getStatus());
        existingProduct.setMessages(updatedProduct.getMessages());
    }


    public ApplicationContext getCtx() {
        return ctx;
    }

    public void setCtx(ApplicationContext ctx) {
        this.ctx = ctx;
    }
}
