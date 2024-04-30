package com.controller.Repositories;

import com.controller.Classes.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM clientes WHERE codigo = ?1", nativeQuery = true)
    void deleteByCode(String code);

    @Query(value = "SELECT * FROM clientes WHERE codigo = ?1", nativeQuery = true)
    Optional<Client> findByCode(String code);
}
