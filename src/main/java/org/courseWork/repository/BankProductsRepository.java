package org.courseWork.repository;

import org.courseWork.model.Client;
import org.courseWork.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BankProductsRepository {
    private final JdbcTemplate jdbcTemplate;

    public BankProductsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Product findById(UUID id) {
        String sql = "SELECT * FROM PUBLIC.PRODUCTS WHERE ID = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id.toString()}, (rs, rownum) -> {
            Product product = new Product();
            product.setId(UUID.fromString(rs.getString("ID")));
            product.setProductId(UUID.fromString(rs.getString("PRODUCT_ID")));
            product.setUserId(UUID.fromString(rs.getString("USER_ID")));
            product.setType(rs.getString("TYPE"));
            product.setAmount(rs.getInt("AMOUNT"));
            return product;
        });
    }
    

    public List<Product> findAll() {
        String sql = "SELECT * FROM PUBLIC.PRODUCTS";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Product product = new Product();
            product.setId(UUID.fromString(rs.getString("ID")));
            product.setProductId(UUID.fromString(rs.getString("PRODUCT_ID")));
            product.setUserId(UUID.fromString(rs.getString("USER_ID")));
            product.setType(rs.getString("TYPE"));
            product.setAmount(rs.getInt("AMOUNT"));
            return product;
        });
    }

    public List<Product> findProductByUserId(UUID userId) {
        String sql = "SELECT pr.NAME, pr.ID, tr.USER_ID, pr.TYPE, tr.PRODUCT_ID " +
                "FROM PUBLIC.PRODUCTS pr " +
                "JOIN PUBLIC.TRANSACTIONS tr ON pr.ID = tr.PRODUCT_ID " +
                "WHERE tr.USER_ID = ? AND pr.TYPE = 'DEBIT' AND tr.TYPE = 'DEPOSIT' " +
                "GROUP BY pr.ID, tr.PRODUCT_ID, tr.USER_ID, pr.TYPE " +
                "HAVING SUM(tr.AMOUNT) > 1000";

        return jdbcTemplate.query(sql, new Object[]{userId.toString()}, (rs, rowNum) -> {
            Product product = new Product();
            product.setId(UUID.fromString(rs.getString("ID")));
            product.setProductId(UUID.fromString(rs.getString("PRODUCT_ID")));
            product.setUserId(UUID.fromString(rs.getString("USER_ID")));
            product.setType(rs.getString("TYPE"));
           // product.setAmount(rs.getInt("AMOUNT"));
            return product;
        });
    }
    }
