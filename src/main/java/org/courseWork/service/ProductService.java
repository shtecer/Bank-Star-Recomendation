package org.courseWork.service;

import org.courseWork.model.Product;
import org.courseWork.repository.BankProductsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    private final BankProductsRepository bankProductsRepository;

    public ProductService(BankProductsRepository bankProductsRepository){

        this.bankProductsRepository = bankProductsRepository;
    }
    public List<Product> findProductByUserId(UUID uuid) {
        return bankProductsRepository.findProductByUserId(uuid);
    }
}
