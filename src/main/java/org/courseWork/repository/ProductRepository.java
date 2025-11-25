package org.courseWork.repository;

import org.courseWork.model.Product;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(@Qualifier("primaryJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Product> productRowMapper = (rs, rowNum) -> {
        Product product = new Product();
        product.setId(UUID.fromString(rs.getString("id")));
        product.setProductName(rs.getString("product_name"));
        product.setDescription(rs.getString("description"));
        return product;
    };

    public List<Product> findAll() {
        String sql = "SELECT id, product_name, description FROM products";
        return jdbcTemplate.query(sql, productRowMapper);
    }

    public Optional<Product> findById(UUID id) {
        String sql = "SELECT id, product_name, description FROM products WHERE id = ?";
        try {
            Product product = jdbcTemplate.queryForObject(sql, productRowMapper, id.toString());
            return Optional.ofNullable(product);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}