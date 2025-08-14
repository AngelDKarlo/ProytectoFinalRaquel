package com.trading.cripto.controller;

import com.trading.cripto.model.Portafolio;
import com.trading.cripto.service.PortafolioService;
import com.trading.cripto.service.TradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/portafolio")
public class PortafolioController {

    @Autowired
    private PortafolioService portafolioService;

    @Autowired
    private TradingService tradingService;

    /**
     * Obtener resumen completo del portafolio
     * GET /api/portafolio/resumen/{userId}
     */
    @GetMapping("/resumen/{userId}")
    public ResponseEntity<Map<String, Object>> obtenerResumenPortafolio(@PathVariable Integer userId) {
        Map<String, Object> resumen = portafolioService.obtenerResumenPortafolio(userId);
        return ResponseEntity.ok(resumen);
    }

    /**
     * Obtener solo saldo USD
     * GET /api/portafolio/saldo/{userId}
     */
    @GetMapping("/saldo/{userId}")
    public ResponseEntity<Portafolio> obtenerSaldo(@PathVariable Integer userId) {
        Portafolio portafolio = tradingService.obtenerSaldoUsuario(userId);
        return ResponseEntity.ok(portafolio);
    }
}