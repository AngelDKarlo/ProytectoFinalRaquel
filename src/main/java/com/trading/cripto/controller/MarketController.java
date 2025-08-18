package com.trading.cripto.controller;

import com.trading.cripto.model.Cryptocurrency;
import com.trading.cripto.repository.CryptocurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/market")
@CrossOrigin(origins = "*")
public class MarketController {

    @Autowired
    private CryptocurrencyRepository cryptoRepo;

    /**
     * Obtener precios - con configuración que funciona
     */
    @GetMapping("/prices")
    public ResponseEntity<?> obtenerPrecios() {
        try {
            System.out.println("📊 [MarketController] Solicitud de precios");
            
            List<Cryptocurrency> cryptos = cryptoRepo.findAll();
            System.out.println("📊 Criptomonedas encontradas: " + cryptos.size());
            
            // Formato idéntico a DebugController
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cryptos_count", cryptos.size());
            response.put("cryptos", cryptos);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ [MarketController] Error: " + e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Test endpoint
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        System.out.println("🧪 [MarketController] Test endpoint");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "MarketController funcionando correctamente");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
