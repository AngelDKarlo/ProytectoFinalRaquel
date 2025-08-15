package com.trading.cripto.controller;

import com.trading.cripto.service.DatabaseDebugService;
import com.trading.cripto.repository.CryptocurrencyRepository;
import com.trading.cripto.repository.PriceHistoryRepository;
import com.trading.cripto.model.Cryptocurrency;
import com.trading.cripto.model.PriceHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class DebugController {

    @Autowired
    private DatabaseDebugService debugService;

    @Autowired
    private CryptocurrencyRepository cryptoRepo;

    @Autowired
    private PriceHistoryRepository priceHistoryRepo;

    /**
     * Test completo de base de datos
     * GET /api/debug/test-db
     */
    @GetMapping("/test-db")
    public ResponseEntity<?> testDatabase() {
        try {
            debugService.testDatabaseInsert();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Test de base de datos ejecutado. Revisa los logs de la consola.");
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
     * Obtener estadísticas de la BD
     * GET /api/debug/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getDatabaseStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Contar registros en cada tabla
            stats.put("cryptos_count", cryptoRepo.count());
            stats.put("price_history_count", priceHistoryRepo.count());
            
            // Obtener criptomonedas
            List<Cryptocurrency> cryptos = cryptoRepo.findAll();
            stats.put("cryptos", cryptos);
            
            // Obtener últimos precios
            List<PriceHistory> recentPrices = priceHistoryRepo.findAll()
                .stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(10)
                .toList();
            stats.put("recent_prices", recentPrices);
            
            stats.put("success", true);
            stats.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            stats.put("success", false);
            stats.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(stats);
        }
    }

    /**
     * Actualizar precios manualmente
     * POST /api/debug/update-prices
     */
    @PostMapping("/update-prices")
    public ResponseEntity<?> updatePrices() {
        try {
            debugService.updateCryptoPricesManually();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Precios actualizados manualmente");
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
     * Limpiar datos de prueba
     * DELETE /api/debug/clean-test-data
     */
    @DeleteMapping("/clean-test-data")
    public ResponseEntity<?> cleanTestData() {
        try {
            debugService.cleanTestData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Datos de prueba eliminados");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Verificar conexión a BD
     * GET /api/debug/connection
     */
    @GetMapping("/connection")
    public ResponseEntity<?> checkConnection() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Intentar hacer una consulta simple
            long count = cryptoRepo.count();
            
            status.put("connected", true);
            status.put("crypto_count", count);
            status.put("message", "Conexión a BD exitosa");
            status.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            status.put("connected", false);
            status.put("error", e.getMessage());
            status.put("message", "Error de conexión a BD");
            
            return ResponseEntity.status(500).body(status);
        }
    }
}
