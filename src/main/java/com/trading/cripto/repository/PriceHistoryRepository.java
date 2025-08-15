package com.trading.cripto.repository;

import com.trading.cripto.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Integer> {

    // Obtener histórico por crypto y rango de fechas
    List<PriceHistory> findByCryptoIdAndTimestampBetweenOrderByTimestampAsc(
            Integer cryptoId, LocalDateTime start, LocalDateTime end);

    // Obtener histórico con intervalo específico
    List<PriceHistory> findByCryptoIdAndIntervaloOrderByTimestampDesc(
            Integer cryptoId, String intervalo);

    // Query personalizado para obtener últimos N registros
    @Query(value = "SELECT * FROM price_history WHERE crypto_id = :cryptoId " +
            "ORDER BY timestamp DESC LIMIT :limit", nativeQuery = true)
    List<PriceHistory> findLastNPrices(@Param("cryptoId") Integer cryptoId,
                                       @Param("limit") int limit);

    // Obtener precio más reciente
    @Query(value = "SELECT * FROM price_history WHERE crypto_id = :cryptoId " +
            "ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    PriceHistory findLatestPrice(@Param("cryptoId") Integer cryptoId);

    // Eliminar registros antiguos (para limpieza)
    void deleteByTimestampBefore(LocalDateTime timestamp);
}
