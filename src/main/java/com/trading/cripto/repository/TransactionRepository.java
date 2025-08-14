package com.trading.cripto.repository;

import com.trading.cripto.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findByUserIdOrderByFechaEjecucionDesc(Integer userId);
    List<Transaction> findByCryptoIdOrderByFechaEjecucionDesc(Integer cryptoId);
    List<Transaction> findByUserIdAndCryptoIdOrderByFechaEjecucionDesc(Integer userId, Integer cryptoId);
}