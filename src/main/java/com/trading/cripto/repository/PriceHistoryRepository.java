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

    // Obtener últimos N registros de una crypto
    List<PriceHistory> findTopNCryptoIdOrderByTimestampDesc(Integer cryptoId, int limit);

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

    // Obtener datos para gráfica de velas (candlestick)
    @Query(value = "SELECT " +
            "MIN(precio) as precio_minimo, " +
            "MAX(precio) as precio_maximo, " +
            "FIRST_VALUE(precio) OVER (ORDER BY timestamp) as precio_apertura, " +
            "LAST_VALUE(precio) OVER (ORDER BY timestamp) as precio_cierre, " +
            "SUM(volumen) as volumen_total, " +
            "DATE_FORMAT(timestamp, '%Y-%m-%d %H:%i:00') as periodo " +
            "FROM price_history " +
            "WHERE crypto_id = :cryptoId " +
            "AND timestamp BETWEEN :start AND :end " +
            "GROUP BY periodo " +
            "ORDER BY periodo", nativeQuery = true)
    List<Object[]> getCandlestickData(@Param("cryptoId") Integer cryptoId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    // Eliminar registros antiguos (para limpieza)
    void deleteByTimestampBefore(LocalDateTime timestamp);
}