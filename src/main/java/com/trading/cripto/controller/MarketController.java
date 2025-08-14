package com.trading.cripto.controller;

import com.trading.cripto.model.Cryptocurrency;
import com.trading.cripto.repository.CryptocurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    @Autowired
    private CryptocurrencyRepository cryptoRepo;

    /**
     * Obtener todas las criptomonedas y sus precios
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
}