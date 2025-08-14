package com.trading.cripto.controller;

import com.trading.cripto.model.Portafolio;
import com.trading.cripto.service.PortafolioService;
import com.trading.cripto.service.TradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/portafolio")
@CrossOrigin(origins = "*")
public class PortafolioController {

    @Autowired
    private PortafolioService portafolioService;

    @Autowired
    private TradingService tradingService;

    /**
     * Obtener resumen completo del portafolio del usuario autenticado
     * GET /api/portafolio/resumen
     */
    @GetMapping("/resumen")
    public ResponseEntity<?> obtenerResumenPortafolio(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body(
                    Map.of("error", "Usuario no autenticado"));
        }

        Map<String, Object> resumen = portafolioService.obtenerResumenPortafolio(userId);
        return ResponseEntity.ok(resumen);
    }

    /**
     * Obtener solo saldo USD del usuario autenticado
     * GET /api/portafolio/saldo
     */
    @GetMapping("/saldo")
    public ResponseEntity<?> obtenerSaldo(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body(
                    Map.of("error", "Usuario no autenticado"));
        }

        Portafolio portafolio = tradingService.obtenerSaldoUsuario(userId);
        return ResponseEntity.ok(portafolio);
    }

    /**
     * Obtener rendimiento del portafolio
     * GET /api/portafolio/rendimiento
     */
    @GetMapping("/rendimiento")
    public ResponseEntity<?> obtenerRendimiento(
            HttpServletRequest request,
            @RequestParam(defaultValue = "24") int hours) {

        Integer userId = (Integer) request.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body(
                    Map.of("error", "Usuario no autenticado"));
        }

        // Aquí podrías calcular el rendimiento basado en el historial
        // Por ahora retornamos datos de ejemplo
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "period", hours + " hours",
                "initialValue", 10000.00,
                "currentValue", 10500.00,
                "profit", 500.00,
                "profitPercentage", 5.0,
                "message", "Cálculo de rendimiento simplificado"
        ));
    }
}