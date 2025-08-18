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
     * GET /api/market/prices - Obtener todos los precios actuales
     */
    @GetMapping("/prices")
    public ResponseEntity<?> obtenerPrecios() {
        try {
            List<Cryptocurrency> cryptos = cryptoRepo.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", cryptos);
            response.put("count", cryptos.size());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * GET /api/market/price/{symbol} - Obtener precio de una criptomoneda específica
     */
    @GetMapping("/price/{symbol}")
    public ResponseEntity<?> obtenerPrecio(@PathVariable String symbol) {
        try {
            Optional<Cryptocurrency> crypto = cryptoRepo.findBySimbolo(symbol.toUpperCase());
            if (crypto.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", crypto.get());
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Criptomoneda no encontrada: " + symbol);
                return ResponseEntity.status(404).body(error);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * GET /api/market/history/{symbol} - Obtener histórico de precios
     */
    @GetMapping("/history/{symbol}")
    public ResponseEntity<?> obtenerHistorial(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "24") int hours) {
        try {
            List<PriceHistory> history = marketDataService.getHistoricalData(symbol, hours);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("symbol", symbol);
            response.put("period_hours", hours);
            response.put("data", history);
            response.put("count", history.size());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * GET /api/market/stats/{symbol} - Obtener estadísticas de mercado
     */
    @GetMapping("/stats/{symbol}")
    public ResponseEntity<?> obtenerEstadisticas(@PathVariable String symbol) {
        try {
            Map<String, Object> stats = marketDataService.getMarketStats(symbol);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * GET /api/market/summary - Resumen general del mercado
     */
    @GetMapping("/summary")
    public ResponseEntity<?> obtenerResumenMercado() {
        try {
            List<Cryptocurrency> cryptos = cryptoRepo.findAll();
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("total_cryptocurrencies", cryptos.size());
            summary.put("market_status", "ACTIVE");
            summary.put("update_interval", "5 seconds");
            
            // Agregar información de cada cripto
            Map<String, Object> cryptoData = new HashMap<>();
            for (Cryptocurrency crypto : cryptos) {
                Map<String, Object> stats = marketDataService.getMarketStats(crypto.getSimbolo());
                cryptoData.put(crypto.getSimbolo(), stats);
            }
            summary.put("cryptocurrencies", cryptoData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", summary);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
