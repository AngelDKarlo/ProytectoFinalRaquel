package com.trading.cripto.repository;

import com.trading.cripto.model.Portafolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PortafolioRepository extends JpaRepository<Portafolio, Integer> {
    Optional<Portafolio> findByUserId(Integer userId);
}