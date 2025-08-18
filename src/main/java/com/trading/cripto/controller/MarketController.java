// MarketController.java - CORS COMPLETAMENTE CORREGIDO
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
@CrossOrigin(
    origins = "*", 
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
    allowCredentials = "false"  // CAMBIO IMPORTANTE: false para evitar conflictos
)
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
    @CrossOrigin(origins = "*") // CORS adicional específico para este endpoint
    public ResponseEntity<List<Cryptocurrency>> obtenerPrecios() {
        try {
            List<Cryptocurrency> cryptos = cryptoRepo.findAll();
            
            return ResponseEntity.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")
                    .header("Access-Control-Allow-Credentials", "false")
                    .body(cryptos);
        } catch (Exception e) {
            System.err.println("Error en /market/prices: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }
    }

    /**
     * Obtener precio de una criptomoneda específica
     * GET /api/market/price/{symbol}
     */
    @GetMapping("/price/{symbol}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Cryptocurrency> obtenerPrecio(@PathVariable String symbol) {
        try {
            Optional<Cryptocurrency> crypto = cryptoRepo.findBySimbolo(symbol.toUpperCase());
            if (crypto.isPresent()) {
                return ResponseEntity.ok()
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                        .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                        .body(crypto.get());
            } else {
                return ResponseEntity.notFound()
                        .header("Access-Control-Allow-Origin", "*")
                        .build();
            }
        } catch (Exception e) {
            System.err.println("Error en /market/price/" + symbol + ": " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }
    }

    /**
     * Obtener histórico de precios
     * GET /api/market/history/{symbol}?hours=24
     */
    @GetMapping("/history/{symbol}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> obtenerHistorico(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "24") int hours) {

        try {
            if (hours > 168) { // Máximo 7 días
                return ResponseEntity.badRequest()
                        .header("Access-Control-Allow-Origin", "*")
                        .body(Map.of("error", "Máximo 168 horas (7 días) permitidas"));
            }

            List<PriceHistory> history = marketDataService.getHistoricalData(symbol.toUpperCase(), hours);

            if (history.isEmpty()) {
                return ResponseEntity.notFound()
                        .header("Access-Control-Allow-Origin", "*")
                        .build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("symbol", symbol.toUpperCase());
            response.put("hours", hours);
            response.put("dataPoints", history.size());
            response.put("history", history);

            return ResponseEntity.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                    .body(response);
        } catch (Exception e) {
            System.err.println("Error en /market/history/" + symbol + ": " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .header("Access-Control-Allow-Origin", "*")
                    .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

    /**
     * OPTIONS para preflight CORS - MUY IMPORTANTE
     */
    @RequestMapping(method = RequestMethod.OPTIONS, value = "/**")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> handlePreflight() {
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With")
                .header("Access-Control-Allow-Credentials", "false")
                .header("Access-Control-Max-Age", "3600")
                .build();
    }

    /**
     * Obtener últimos N precios
     * GET /api/market/recent/{symbol}?limit=100
     */
    @GetMapping("/recent/{symbol}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> obtenerRecientes(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "100") int limit) {

        try {
            if (limit > 1000) {
                return ResponseEntity.badRequest()
                        .header("Access-Control-Allow-Origin", "*")
                        .body(Map.of("error", "Máximo 1000 registros permitidos"));
            }

            List<PriceHistory> prices = marketDataService.getLastNPrices(symbol.toUpperCase(), limit);

            Map<String, Object> response = new HashMap<>();
            response.put("symbol", symbol.toUpperCase());
            response.put("limit", limit);
            response.put("prices", prices);

            return ResponseEntity.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                    .body(response);
        } catch (Exception e) {
            System.err.println("Error en /market/recent/" + symbol + ": " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .header("Access-Control-Allow-Origin", "*")
                    .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

    /**
     * Obtener estadísticas del mercado
     * GET /api/market/stats/{symbol}
     */
    @GetMapping("/stats/{symbol}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> obtenerEstadisticas(@PathVariable String symbol) {
        try {
            Map<String, Object> stats = marketDataService.getMarketStats(symbol.toUpperCase());

            if (stats.isEmpty()) {
                return ResponseEntity.notFound()
                        .header("Access-Control-Allow-Origin", "*")
                        .build();
            }

            return ResponseEntity.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                    .body(stats);
        } catch (Exception e) {
            System.err.println("Error en /market/stats/" + symbol + ": " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .header("Access-Control-Allow-Origin", "*")
                    .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

    /**
     * Obtener resumen del mercado (todas las cryptos)
     * GET /api/market/summary
     */
    @GetMapping("/summary")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> obtenerResumenMercado() {
        try {
            List<Cryptocurrency> cryptos = cryptoRepo.findAll();
            Map<String, Object> summary = new HashMap<>();

            for (Cryptocurrency crypto : cryptos) {
                Map<String, Object> cryptoStats = marketDataService.getMarketStats(crypto.getSimbolo());
                summary.put(crypto.getSimbolo(), cryptoStats);
            }

            return ResponseEntity.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                    .body(summary);
        } catch (Exception e) {
            System.err.println("Error en /market/summary: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .header("Access-Control-Allow-Origin", "*")
                    .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }
}
