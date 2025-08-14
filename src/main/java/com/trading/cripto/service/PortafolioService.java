package com.trading.cripto.service;

import com.trading.cripto.model.*;
import com.trading.cripto.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PortafolioService {

    @Autowired
    private PortafolioRepository portafolioRepo;

    @Autowired
    private WalletRepository walletRepo;

    @Autowired
    private CryptocurrencyRepository cryptoRepo;

    @Autowired
    private TransactionRepository transactionRepo;

    /**
     * Obtiene el resumen completo del portafolio
     */
    public Map<String, Object> obtenerResumenPortafolio(Integer userId) {
        Map<String, Object> resumen = new HashMap<>();

        // Saldo USD
        Portafolio portafolio = portafolioRepo.findByUserId(userId)
                .orElse(new Portafolio(userId, new BigDecimal("10000.00")));

        resumen.put("saldoUsd", portafolio.getSaldoUsd());

        // Wallets de cripto
        List<Wallet> wallets = walletRepo.findByUserId(userId);
        List<Map<String, Object>> criptoHoldings = new ArrayList<>();
        BigDecimal valorTotalCripto = BigDecimal.ZERO;

        for (Wallet wallet : wallets) {
            if (wallet.getSaldo().compareTo(BigDecimal.ZERO) > 0) {
                Optional<Cryptocurrency> cryptoOpt = cryptoRepo.findById(wallet.getCryptoId());
                if (cryptoOpt.isPresent()) {
                    Cryptocurrency crypto = cryptoOpt.get();
                    BigDecimal valorActual = wallet.getSaldo().multiply(crypto.getPrecio());

                    Map<String, Object> holding = new HashMap<>();
                    holding.put("simbolo", crypto.getSimbolo());
                    holding.put("nombre", crypto.getNombre());
                    holding.put("cantidad", wallet.getSaldo());
                    holding.put("precioActual", crypto.getPrecio());
                    holding.put("valorTotal", valorActual);

                    criptoHoldings.add(holding);
                    valorTotalCripto = valorTotalCripto.add(valorActual);
                }
            }
        }

        resumen.put("criptoHoldings", criptoHoldings);
        resumen.put("valorTotalCripto", valorTotalCripto);
        resumen.put("valorTotalPortafolio", portafolio.getSaldoUsd().add(valorTotalCripto));

        // Últimas 10 transacciones
        List<Transaction> ultimasTransacciones = transactionRepo
                .findByUserIdOrderByFechaEjecucionDesc(userId)
                .stream()
                .limit(10)
                .toList();
        resumen.put("ultimasTransacciones", ultimasTransacciones);

        return resumen;
    }
}
