package com.trading.cripto.repository;

import com.trading.cripto.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {
    
    Optional<Wallet> findByUserIdAndCryptoId(Integer userId, Integer cryptoId);
    
    List<Wallet> findByUserId(Integer userId);
    
    List<Wallet> findByCryptoId(Integer cryptoId);
}