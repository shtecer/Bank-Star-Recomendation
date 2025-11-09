package org.courseWork.repository;

import org.courseWork.dto.Client;

import java.util.Optional;

public interface UserRepository {
    Optional<Client> findById(Long id);
    boolean existsById(Long id);
}
