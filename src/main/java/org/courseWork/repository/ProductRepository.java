package org.courseWork.repository;

import org.courseWork.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(Long id);
    List<Product> findByIds(List<Long> ids);
    List<Product> findAllActive();
}
