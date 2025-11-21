package org.courseWork.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.courseWork.model.Product;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ProductRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public ProductRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    private final RowMapper<Product> productRowMapper = (rs, rowNum) ->
            new Product(
                    UUID.fromString(rs.getString("id")),
                    rs.getString("product_name"),
                    rs.getString("description")
            );

    public List<Product> findAllActive() {
        String sql = "SELECT id, product_name, description FROM products WHERE active = true";
        return jdbcTemplate.query(sql, productRowMapper);
    }

    public Optional<Product> findById(UUID productId) {
        String sql = "SELECT id, product_name, description FROM products WHERE id = ? AND active = true";
        try {
            Product product = jdbcTemplate.queryForObject(sql, productRowMapper, productId.toString());
            return Optional.ofNullable(product);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Product> findByIds(List<UUID> productIds) {
        if (productIds.isEmpty()) {
            return new ArrayList<>();
        }

        String sql = "SELECT id, product_name, description FROM products WHERE id IN (" +
                String.join(",", Collections.nCopies(productIds.size(), "?")) + ") AND active = true";

        List<String> productIdStrings = productIds.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());

        return jdbcTemplate.query(sql, productRowMapper, productIdStrings.toArray());
    }
}

