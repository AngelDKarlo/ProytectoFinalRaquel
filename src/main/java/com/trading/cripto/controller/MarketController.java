package com.trading.cripto.controller;

import com.trading.cripto.model.Cryptocurrency;
import com.trading.cripto.model.PriceHistory;
import com.trading.cripto.repository.CryptocurrencyRepository;
import com.trading.cripto.service.MarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/market")
@CrossOrigin(origins = "*")
public class MarketController {

    @Autowired
    private CryptocurrencyRepository cryptoRepo;

    @Autowired
    private MarketDataService marketDataService;

    /**
     * Obtener todas las criptomonedas y sus precios actuales
     * GET /api/market/prices
     */
    @GetMapping("/prices")
    public ResponseEntity<List<Cryptocurrency>> obtenerPrecios() {
        List<Cryptocurrency> cryptos = cryptoRepo.findAll();
        return ResponseEntity.ok(cryptos);
    }

    /**
     * Obtener precio de una criptomoneda específica
     * GET /api/market/price/{symbol}
     */
    @GetMapping("/price/{symbol}")
    public ResponseEntity<Cryptocurrency> obtenerPrecio(@PathVariable String symbol) {
        Optional<Cryptocurrency> crypto = cryptoRepo.findBySimbolo(symbol.toUpperCase());
        if (crypto.isPresent()) {
            return ResponseEntity.ok(crypto.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtener histórico de precios
     * GET /api/market/history/{symbol}?hours=24
     */
    @GetMapping("/history/{symbol}")
    public ResponseEntity<?> obtenerHistorico(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "24") int hours) {

        if (hours > 168) { // Máximo 7 días
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Máximo 168 horas (7 días) permitidas"));
        }

        List<PriceHistory> history = marketDataService.getHistoricalData(symbol.toUpperCase(), hours);

        if (history.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("symbol", symbol.toUpperCase());
        response.put("hours", hours);
        response.put("dataPoints", history.size());
        response.put("history", history);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener últimos N precios
     * GET /api/market/recent/{symbol}?limit=100
     */
    @GetMapping("/recent/{symbol}")
    public ResponseEntity<?> obtenerRecientes(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "100") int limit) {

        if (limit > 1000) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Máximo 1000 registros permitidos"));
        }

        List<PriceHistory> prices = marketDataService.getLastNPrices(symbol.toUpperCase(), limit);

        Map<String, Object> response = new HashMap<>();
        response.put("symbol", symbol.toUpperCase());
        response.put("limit", limit);
        response.put("prices", prices);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener estadísticas del mercado
     * GET /api/market/stats/{symbol}
     */
    @GetMapping("/stats/{symbol}")
    public ResponseEntity<?> obtenerEstadisticas(@PathVariable String symbol) {
        Map<String, Object> stats = marketDataService.getMarketStats(symbol.toUpperCase());

        if (stats.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(stats);
    }

    /**
     * Obtener resumen del mercado (todas las cryptos)
     * GET /api/market/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<?> obtenerResumenMercado() {
        List<Cryptocurrency> cryptos = cryptoRepo.findAll();
        Map<String, Object> summary = new HashMap<>();

        for (Cryptocurrency crypto : cryptos) {
            Map<String, Object> cryptoStats = marketDataService.getMarketStats(crypto.getSimbolo());
            summary.put(crypto.getSimbolo(), cryptoStats);
        }

        return ResponseEntity.ok(summary);
    }
}