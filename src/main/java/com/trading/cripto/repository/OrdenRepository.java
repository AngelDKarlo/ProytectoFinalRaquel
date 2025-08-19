package com.trading.cripto.repository;

import com.trading.cripto.model.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Integer> {
    List<Orden> findByUserId(Integer userId);
    List<Orden> findByUserIdAndEstado(Integer userId, String estado);
}
