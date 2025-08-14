package com.trading.cripto.service;

import com.trading.cripto.model.Cryptocurrency;
import com.trading.cripto.model.PriceHistory;
import com.trading.cripto.model.PriceTick;
import com.trading.cripto.repository.CryptocurrencyRepository;
import com.trading.cripto.repository.PriceHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MarketDataService {

    @Autowired
    private CsvImportService csvImportService;

    @Autowired
    private CryptocurrencyRepository cryptoRepo;

    @Autowired
    private PriceHistoryRepository priceHistoryRepo;

    // Almacén para los datos históricos en memoria
    private final Map<String, List<PriceTick>> historicalData = new ConcurrentHashMap<>();
    // Índice para saber en qué punto de la simulación vamos para cada moneda
    private final Map<String, AtomicInteger> currentTickIndex = new ConcurrentHashMap<>();
    // Precios actuales para generar variaciones realistas
    private final Map<String, BigDecimal> currentPrices = new ConcurrentHashMap<>();
    // Volatilidad por moneda (porcentaje de cambio máximo)
    private final Map<String, Double> volatility = new ConcurrentHashMap<>();

    // Este método se ejecuta después de que el bean ha sido creado
    @PostConstruct
    public void loadData() {
        System.out.println("🚀 Cargando datos históricos del mercado...");

        // Configurar volatilidad para cada cripto
        volatility.put("ZOR", 0.05); // 5% volatilidad
        volatility.put("NEB", 0.08); // 8% volatilidad
        volatility.put("LUM", 0.03); // 3% volatilidad

        // Precios iniciales
        currentPrices.put("ZOR", new BigDecimal("12.58"));
        currentPrices.put("NEB", new BigDecimal("8.34"));
        currentPrices.put("LUM", new BigDecimal("45.67"));

        // Cargar datos CSV si existen
        loadCurrencyData("ZOR", "/historical_data/ZOR_USD_historical.csv");
        loadCurrencyData("NEB", "/historical_data/NEB_USD_historical.csv");
        loadCurrencyData("LUM", "/historical_data/LUM_USD_historical.csv");

        // Si no hay suficientes datos, generar datos sintéticos
        generateSyntheticDataIfNeeded();

        System.out.println("✅ Datos históricos cargados.");

        // Limpiar datos antiguos de la BD (más de 7 días)
        cleanOldPriceHistory();
    }

    private void loadCurrencyData(String symbol, String path) {
        try {
            List<PriceTick> data = csvImportService.importData(path);
            if (data != null && !data.isEmpty()) {
                historicalData.put(symbol, data);
                currentTickIndex.put(symbol, new AtomicInteger(0));

                // Actualizar precio actual con el último del CSV
                PriceTick lastTick = data.get(data.size() - 1);
                currentPrices.put(symbol, lastTick.getPrice());
            }
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo cargar CSV para " + symbol + ": " + e.getMessage());
            generateSyntheticData(symbol, 1000); // Generar 1000 puntos de datos
        }
    }

    private void generateSyntheticDataIfNeeded() {
        for (String symbol : Arrays.asList("ZOR", "NEB", "LUM")) {
            if (!historicalData.containsKey(symbol) || historicalData.get(symbol).size() < 100) {
                System.out.println("📊 Generando datos sintéticos para " + symbol);
                generateSyntheticData(symbol, 1000);
            }
        }
    }

    private void generateSyntheticData(String symbol, int dataPoints) {
        List<PriceTick> syntheticData = new ArrayList<>();
        BigDecimal basePrice = currentPrices.get(symbol);
        LocalDateTime timestamp = LocalDateTime.now().minusHours(dataPoints / 12); // ~5 min intervals

        BigDecimal currentPrice = basePrice;
        Random random = new Random();

        for (int i = 0; i < dataPoints; i++) {
            // Generar cambio de precio realista usando random walk
            double changePercent = (random.nextGaussian() * volatility.get(symbol)) / 10;
            BigDecimal change = currentPrice.multiply(BigDecimal.valueOf(changePercent));
            currentPrice = currentPrice.add(change);

            // Prevenir precios negativos
            if (currentPrice.compareTo(BigDecimal.ONE) < 0) {
                currentPrice = BigDecimal.ONE;
            }

            syntheticData.add(new PriceTick(timestamp, currentPrice));
            timestamp = timestamp.plusMinutes(5);
        }

        historicalData.put(symbol, syntheticData);
        currentTickIndex.put(symbol, new AtomicInteger(0));
    }

    // Tarea programada que se ejecuta cada 5 segundos
    @Scheduled(fixedRate = 5000)
    public void publishNextPriceTick() {
        LocalDateTime now = LocalDateTime.now();

        historicalData.forEach((symbol, prices) -> {
            try {
                BigDecimal newPrice = calculateNextPrice(symbol);

                System.out.println(String.format("💹 %s: $%.4f (%.2f%%)",
                        symbol,
                        newPrice,
                        calculateChangePercent(symbol, newPrice)));

                // Actualizar precio en la base de datos
                cryptoRepo.findBySimbolo(symbol).ifPresent(crypto -> {
                    crypto.setPrecio(newPrice);
                    cryptoRepo.save(crypto);

                    // Guardar en histórico
                    savePriceHistory(crypto.getId(), newPrice, now);
                });

                currentPrices.put(symbol, newPrice);

            } catch (Exception e) {
                System.err.println("❌ Error actualizando precio para " + symbol + ": " + e.getMessage());
            }
        });
    }

    private BigDecimal calculateNextPrice(String symbol) {
        // Combinar datos históricos con variación aleatoria
        AtomicInteger indexRef = currentTickIndex.get(symbol);
        List<PriceTick> prices = historicalData.get(symbol);

        if (indexRef != null && prices != null && !prices.isEmpty()) {
            int index = indexRef.getAndIncrement();

            // Si llegamos al final, reiniciar con variación
            if (index >= prices.size()) {
                index = 0;
                indexRef.set(0);
            }

            PriceTick historicalTick = prices.get(index);
            BigDecimal historicalPrice = historicalTick.getPrice();

            // Añadir ruido aleatorio para hacerlo más realista
            double noise = ThreadLocalRandom.current().nextGaussian() * 0.01; // 1% ruido
            BigDecimal variation = historicalPrice.multiply(BigDecimal.valueOf(noise));

            return historicalPrice.add(variation).setScale(4, RoundingMode.HALF_UP);
        }

        // Fallback: generar precio con random walk
        BigDecimal currentPrice = currentPrices.get(symbol);
        double changePercent = ThreadLocalRandom.current().nextGaussian() * volatility.get(symbol) / 10;
        BigDecimal change = currentPrice.multiply(BigDecimal.valueOf(changePercent));

        return currentPrice.add(change).setScale(4, RoundingMode.HALF_UP);
    }

    private double calculateChangePercent(String symbol, BigDecimal newPrice) {
        BigDecimal oldPrice = currentPrices.get(symbol);
        if (oldPrice == null || oldPrice.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        return newPrice.subtract(oldPrice)
                .divide(oldPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    private void savePriceHistory(Integer cryptoId, BigDecimal price, LocalDateTime timestamp) {
        try {
            PriceHistory history = new PriceHistory(cryptoId, price);
            history.setTimestamp(timestamp);
            history.setIntervalo("5s"); // Intervalo de 5 segundos

            // Simular volumen
            BigDecimal volume = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(1000, 50000));
            history.setVolumen(volume);

            priceHistoryRepo.save(history);
        } catch (Exception e) {
            System.err.println("Error guardando histórico: " + e.getMessage());
        }
    }

    private void cleanOldPriceHistory() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
            priceHistoryRepo.deleteByTimestampBefore(cutoffDate);
            System.out.println("🧹 Limpieza de datos antiguos completada");
        } catch (Exception e) {
            System.err.println("Error limpiando datos antiguos: " + e.getMessage());
        }
    }

    /**
     * Obtener datos históricos para gráficas
     */
    public List<PriceHistory> getHistoricalData(String symbol, int hours) {
        Optional<Cryptocurrency> crypto = cryptoRepo.findBySimbolo(symbol);
        if (crypto.isEmpty()) {
            return new ArrayList<>();
        }

        LocalDateTime start = LocalDateTime.now().minusHours(hours);
        LocalDateTime end = LocalDateTime.now();

        return priceHistoryRepo.findByCryptoIdAndTimestampBetweenOrderByTimestampAsc(
                crypto.get().getId(), start, end);
    }

    /**
     * Obtener últimos N precios
     */
    public List<PriceHistory> getLastNPrices(String symbol, int limit) {
        Optional<Cryptocurrency> crypto = cryptoRepo.findBySimbolo(symbol);
        if (crypto.isEmpty()) {
            return new ArrayList<>();
        }

        return priceHistoryRepo.findLastNPrices(crypto.get().getId(), limit);
    }

    /**
     * Obtener estadísticas del mercado
     */
    public Map<String, Object> getMarketStats(String symbol) {
        Map<String, Object> stats = new HashMap<>();
        Optional<Cryptocurrency> crypto = cryptoRepo.findBySimbolo(symbol);

        if (crypto.isEmpty()) {
            return stats;
        }

        BigDecimal currentPrice = crypto.get().getPrecio();
        stats.put("symbol", symbol);
        stats.put("currentPrice", currentPrice);
        stats.put("volatility", volatility.get(symbol));

        // Calcular cambio 24h
        LocalDateTime dayAgo = LocalDateTime.now().minusHours(24);
        List<PriceHistory> dayHistory = priceHistoryRepo.findByCryptoIdAndTimestampBetweenOrderByTimestampAsc(
                crypto.get().getId(), dayAgo, LocalDateTime.now());

        if (!dayHistory.isEmpty()) {
            BigDecimal openPrice = dayHistory.get(0).getPrecio();
            BigDecimal change24h = currentPrice.subtract(openPrice);
            BigDecimal changePercent = change24h.divide(openPrice, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            stats.put("change24h", change24h);
            stats.put("changePercent24h", changePercent);
            stats.put("high24h", dayHistory.stream()
                    .map(PriceHistory::getPrecio)
                    .max(BigDecimal::compareTo)
                    .orElse(currentPrice));
            stats.put("low24h", dayHistory.stream()
                    .map(PriceHistory::getPrecio)
                    .min(BigDecimal::compareTo)
                    .orElse(currentPrice));
        }

        return stats;
    }
}