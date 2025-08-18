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
    allowCredentials = "false"
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
    @CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public ResponseEntity<List<Cryptocurrency>> obtenerPrecios() {
        try {
            System.out.println("🔍 [MarketController] Solicitud recibida en /api/market/prices");
            
            List<Cryptocurrency> cryptos = cryptoRepo.findAll();
            System.out.println("📊 [MarketController] Criptomonedas encontradas: " + cryptos.size());
            
            return ResponseEntity.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")
                    .header("Access-Control-Allow-Credentials", "false")
                    .body(cryptos);
        } catch (Exception e) {
            System.err.println("❌ [MarketController] Error en /market/prices: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                    .build();
        }
    }

    /**
     * OPTIONS para preflight CORS - MUY IMPORTANTE para /market/prices
     */
    @RequestMapping(method = RequestMethod.OPTIONS, value = "/prices")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> handlePreflightPrices() {
        System.out.println("🔄 [MarketController] OPTIONS request recibido para /prices");
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With")
                .header("Access-Control-Allow-Credentials", "false")
                .header("Access-Control-Max-Age", "3600")
                .build();
    }

    /**
     * OPTIONS general para todos los endpoints
     */
    @RequestMapping(method = RequestMethod.OPTIONS, value = "/**")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> handlePreflightGeneral() {
        System.out.println("🔄 [MarketController] OPTIONS request recibido para endpoint general");
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With")
                .header("Access-Control-Allow-Credentials", "false")
                .header("Access-Control-Max-Age", "3600")
                .build();
    }

    /**
     * Obtener precio de una criptomoneda específica
     * GET /api/market/price/{symbol}
     */
    @GetMapping("/price/{symbol}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Cryptocurrency> obtenerPrecio(@PathVariable String symbol) {
        try {
            System.out.println("🔍 [MarketController] Solicitud para precio de: " + symbol);
            Optional<Cryptocurrency> crypto = cryptoRepo.findBySimbolo(symbol.toUpperCase());
            if (crypto.isPresent()) {
                return ResponseEntity.ok()
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "GET, OPTIONS")
                        .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                        .body(crypto.get());
            } else {
                return ResponseEntity.notFound()
                        .header("Access-Control-Allow-Origin", "*")
                        .build();
            }
        } catch (Exception e) {
            System.err.println("❌ [MarketController] Error en /market/price/" + symbol + ": " + e.getMessage());
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
            System.out.println("🔍 [MarketController] Solicitud de histórico para: " + symbol);
            
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
                    .header("Access-Control-Allow-Methods", "GET, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                    .body(response);
        } catch (Exception e) {
            System.err.println("❌ [MarketController] Error en /market/history/" + symbol + ": " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .header("Access-Control-Allow-Origin", "*")
                    .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

    /**
     * Obtener resumen del mercado (todas las cryptos) - NUEVO ENDPOINT
     * GET /api/market/summary
     */
    @GetMapping("/summary")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> obtenerResumenMercado() {
        try {
            System.out.println("🔍 [MarketController] Solicitud de resumen del mercado");
            
            List<Cryptocurrency> cryptos = cryptoRepo.findAll();
            Map<String, Object> summary = new HashMap<>();
            summary.put("timestamp", System.currentTimeMillis());
            summary.put("cryptos", cryptos);
            summary.put("count", cryptos.size());

            return ResponseEntity.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                    .body(summary);
        } catch (Exception e) {
            System.err.println("❌ [MarketController] Error en /market/summary: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .header("Access-Control-Allow-Origin", "*")
                    .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

    /**
     * Test endpoint simple
     * GET /api/market/test
     */
    @GetMapping("/test")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> test() {
        System.out.println("🧪 [MarketController] Test endpoint accedido");
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type")
                .body(Map.of(
                    "status", "OK",
                    "message", "MarketController funcionando",
                    "timestamp", System.currentTimeMillis()
                ));
    }
}
