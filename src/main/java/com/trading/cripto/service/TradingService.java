package com.trading.cripto.service;

import com.trading.cripto.dto.TradeRequest;
import com.trading.cripto.dto.TradeResponse;
import com.trading.cripto.exception.InsufficientFundsExeption;
import com.trading.cripto.exception.TradingExeption;
import com.trading.cripto.model.*;
import com.trading.cripto.model.enums.TransactionType;
import com.trading.cripto.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class TradingService {

    @Autowired
    private CryptocurrencyRepository cryptoRepo;

    @Autowired
    private PortafolioRepository portafolioRepo;

    @Autowired
    private WalletRepository walletRepo;

    @Autowired
    private TransactionRepository transactionRepo;

    @Autowired
    private UserRepository userRepo;

    // Comisión por transacción (0.1%)
    private final BigDecimal COMISION_PORCENTAJE = new BigDecimal("0.001");

    /**
     * Ejecuta una operación de trading (compra o venta)
     */
    @Transactional
    public TradeResponse ejecutarTrade(Integer userId, TradeRequest request) {
        try {
            // Validar datos básicos
            validarRequest(request);

            // Verificar que el usuario existe
            if (!userRepo.existsById(userId)) {
                return new TradeResponse(false, "Usuario no encontrado");
            }

            // Obtener la criptomoneda
            Optional<Cryptocurrency> cryptoOpt = cryptoRepo.findBySimbolo(request.getSymboloCripto());
            if (cryptoOpt.isEmpty()) {
                return new TradeResponse(false, "Criptomoneda no encontrada: " + request.getSymboloCripto());
            }

            Cryptocurrency crypto = cryptoOpt.get();
            BigDecimal precioActual = crypto.getPrecio();

            if (precioActual == null || precioActual.compareTo(BigDecimal.ZERO) <= 0) {
                return new TradeResponse(false, "Precio no disponible para " + request.getSymboloCripto());
            }

            // Ejecutar según tipo de operación
            if ("COMPRA".equals(request.getTipoOperacion())) {
                return ejecutarCompra(userId, crypto, request.getCantidad(), precioActual);
            } else if ("VENTA".equals(request.getTipoOperacion())) {
                return ejecutarVenta(userId, crypto, request.getCantidad(), precioActual);
            } else {
                return new TradeResponse(false, "Tipo de operación no válido: " + request.getTipoOperacion());
            }

        } catch (InsufficientFundsExeption e) {
            return new TradeResponse(false, "Fondos insuficientes: " + e.getMessage());
        } catch (TradingExeption e) {
            return new TradeResponse(false, "Error de trading: " + e.getMessage());
        } catch (Exception e) {
            return new TradeResponse(false, "Error interno: " + e.getMessage());
        }
    }

    /**
     * Ejecuta una compra de criptomoneda
     */
    private TradeResponse ejecutarCompra(Integer userId, Cryptocurrency crypto,
                                         BigDecimal cantidad, BigDecimal precioActual) {

        // Calcular costo total
        BigDecimal costoSinComision = cantidad.multiply(precioActual);
        BigDecimal comision = costoSinComision.multiply(COMISION_PORCENTAJE);
        BigDecimal costoTotal = costoSinComision.add(comision);

        // Verificar saldo USD
        Portafolio portafolio = obtenerOCrearPortafolio(userId);
        if (portafolio.getSaldoUsd().compareTo(costoTotal) < 0) {
            throw new InsufficientFundsExeption(
                    String.format("Saldo insuficiente. Necesario: $%.2f, Disponible: $%.2f",
                            costoTotal, portafolio.getSaldoUsd())
            );
        }

        // Actualizar saldo USD
        portafolio.setSaldoUsd(portafolio.getSaldoUsd().subtract(costoTotal));
        portafolioRepo.save(portafolio);

        // Actualizar o crear wallet de cripto
        Wallet wallet = obtenerOCrearWallet(userId, crypto.getId());
        wallet.setSaldo(wallet.getSaldo().add(cantidad));
        walletRepo.save(wallet);

        // Registrar transacción
        Transaction transaction = new Transaction(
                userId, crypto.getId(), TransactionType.COMPRA.getValue(),
                cantidad, precioActual
        );
        transaction.setComision(comision);
        transactionRepo.save(transaction);

        // Preparar respuesta
        TradeResponse response = new TradeResponse(true, "Compra ejecutada exitosamente");
        response.setCantidadEjecutada(cantidad);
        response.setPrecioEjecutado(precioActual);
        response.setComision(comision);
        response.setNuevoSaldoUsd(portafolio.getSaldoUsd());
        response.setNuevoSaldoCripto(wallet.getSaldo());

        return response;
    }

    /**
     * Ejecuta una venta de criptomoneda
     */
    private TradeResponse ejecutarVenta(Integer userId, Cryptocurrency crypto,
                                        BigDecimal cantidad, BigDecimal precioActual) {

        // Verificar saldo de cripto
        Optional<Wallet> walletOpt = walletRepo.findByUserIdAndCryptoId(userId, crypto.getId());
        if (walletOpt.isEmpty() || walletOpt.get().getSaldo().compareTo(cantidad) < 0) {
            BigDecimal saldoActual = walletOpt.map(Wallet::getSaldo).orElse(BigDecimal.ZERO);
            throw new InsufficientFundsExeption(
                    String.format("Cantidad insuficiente de %s. Necesario: %.8f, Disponible: %.8f",
                            crypto.getSimbolo(), cantidad, saldoActual)
            );
        }

        Wallet wallet = walletOpt.get();

        // Calcular ganancia
        BigDecimal ingresoSinComision = cantidad.multiply(precioActual);
        BigDecimal comision = ingresoSinComision.multiply(COMISION_PORCENTAJE);
        BigDecimal ingresoNeto = ingresoSinComision.subtract(comision);

        // Actualizar saldo cripto
        wallet.setSaldo(wallet.getSaldo().subtract(cantidad));
        walletRepo.save(wallet);

        // Actualizar saldo USD
        Portafolio portafolio = obtenerOCrearPortafolio(userId);
        portafolio.setSaldoUsd(portafolio.getSaldoUsd().add(ingresoNeto));
        portafolioRepo.save(portafolio);

        // Registrar transacción
        Transaction transaction = new Transaction(
                userId, crypto.getId(), TransactionType.VENTA.getValue(),
                cantidad, precioActual
        );
        transaction.setComision(comision);
        transactionRepo.save(transaction);

        // Preparar respuesta
        TradeResponse response = new TradeResponse(true, "Venta ejecutada exitosamente");
        response.setCantidadEjecutada(cantidad);
        response.setPrecioEjecutado(precioActual);
        response.setComision(comision);
        response.setNuevoSaldoUsd(portafolio.getSaldoUsd());
        response.setNuevoSaldoCripto(wallet.getSaldo());

        return response;
    }

    /**
     * Obtiene o crea el portafolio de un usuario
     */
    private Portafolio obtenerOCrearPortafolio(Integer userId) {
        return portafolioRepo.findByUserId(userId)
                .orElseGet(() -> {
                    Portafolio nuevoPortafolio = new Portafolio(userId, new BigDecimal("10000.00"));
                    return portafolioRepo.save(nuevoPortafolio);
                });
    }

    /**
     * Obtiene o crea el wallet de cripto para un usuario
     */
    private Wallet obtenerOCrearWallet(Integer userId, Integer cryptoId) {
        return walletRepo.findByUserIdAndCryptoId(userId, cryptoId)
                .orElseGet(() -> {
                    Wallet nuevoWallet = new Wallet(userId, cryptoId, BigDecimal.ZERO);
                    return walletRepo.save(nuevoWallet);
                });
    }

    /**
     * Valida el request de trading
     */
    private void validarRequest(TradeRequest request) {
        if (request.getSymboloCripto() == null || request.getSymboloCripto().trim().isEmpty()) {
            throw new TradingExeption("Símbolo de criptomoneda requerido");
        }

        if (request.getTipoOperacion() == null ||
                (!request.getTipoOperacion().equals("COMPRA") && !request.getTipoOperacion().equals("VENTA"))) {
            throw new TradingExeption("Tipo de operación debe ser COMPRA o VENTA");
        }

        if (request.getCantidad() == null || request.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TradingExeption("La cantidad debe ser mayor a cero");
        }
    }

    /**
     * Obtiene el saldo actual de un usuario
     */
    public Portafolio obtenerSaldoUsuario(Integer userId) {
        return obtenerOCrearPortafolio(userId);
    }

    /**
     * Obtiene todas las wallets de un usuario
     */
    public java.util.List<Wallet> obtenerWalletsUsuario(Integer userId) {
        return walletRepo.findByUserId(userId);
    }

    /**
     * Obtiene el historial de transacciones de un usuario
     */
    public java.util.List<Transaction> obtenerHistorialTransacciones(Integer userId) {
        return transactionRepo.findByUserIdOrderByFechaEjecucionDesc(userId);
    }
}