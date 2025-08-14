package com.trading.cripto.controller;

import com.trading.cripto.dto.TradeRequest;
import com.trading.cripto.dto.TradeResponse;
import com.trading.cripto.model.Transaction;
import com.trading.cripto.model.Wallet;
import com.trading.cripto.service.TradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trading")
public class TradingController {

    @Autowired
    private TradingService tradingService;

    /**
     * Ejecutar una operación de trading
     * POST /api/trading/execute/{userId}
     */
    @PostMapping("/execute/{userId}")
    public ResponseEntity<TradeResponse> ejecutarTrade(
            @PathVariable Integer userId,
            @RequestBody TradeRequest request) {

        TradeResponse response = tradingService.ejecutarTrade(userId, request);

        if (response.isExitoso()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Obtener wallets del usuario
     * GET /api/trading/wallets/{userId}
     */
    @GetMapping("/wallets/{userId}")
    public ResponseEntity<List<Wallet>> obtenerWallets(@PathVariable Integer userId) {
        List<Wallet> wallets = tradingService.obtenerWalletsUsuario(userId);
        return ResponseEntity.ok(wallets);
    }

    /**
     * Obtener historial de transacciones
     * GET /api/trading/history/{userId}
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<Transaction>> obtenerHistorial(@PathVariable Integer userId) {
        List<Transaction> transacciones = tradingService.obtenerHistorialTransacciones(userId);
        return ResponseEntity.ok(transacciones);
    }
}