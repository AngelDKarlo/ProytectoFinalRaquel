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
import java.sql.Timestamp;
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

    @Autowired
    private OrdenRepository ordenRepo;

    // Comisión por transacción (0.1%)
    private final BigDecimal COMISION_PORCENTAJE = new BigDecimal("0.001");

    /**
     * Ejecuta una operación de trading (compra o venta)
     */
    @Transactional
    public TradeResponse ejecutarTrade(Integer userId, TradeRequest request) {
        System.out.println("🔄 [TradingService] Iniciando trade para userId: " + userId);
        System.out.println("🔄 [TradingService] Request: " + request);

        try {
            // Validar datos básicos
            validarRequest(request);

            // Verificar que el usuario existe
            if (!userRepo.existsById(userId)) {
                System.err.println("❌ [TradingService] Usuario no encontrado: " + userId);
                return new TradeResponse(false, "Usuario no encontrado");
            }

            // Obtener la criptomoneda
            Optional<Cryptocurrency> cryptoOpt = cryptoRepo.findBySimbolo(request.getSymboloCripto());
            if (cryptoOpt.isEmpty()) {
                System.err.println("❌ [TradingService] Criptomoneda no encontrada: " + request.getSymboloCripto());
                return new TradeResponse(false, "Criptomoneda no encontrada: " + request.getSymboloCripto());
            }

            Cryptocurrency crypto = cryptoOpt.get();
            BigDecimal precioActual = crypto.getPrecio();

            if (precioActual == null || precioActual.compareTo(BigDecimal.ZERO) <= 0) {
                System.err.println("❌ [TradingService] Precio no disponible para: " + request.getSymboloCripto());
                return new TradeResponse(false, "Precio no disponible para " + request.getSymboloCripto());
            }

            System.out.println("✅ [TradingService] Datos validados. Precio actual: " + precioActual);

            // Ejecutar según tipo de operación
            if ("COMPRA".equals(request.getTipoOperacion())) {
                return ejecutarCompra(userId, crypto, request.getCantidad(), precioActual);
            } else if ("VENTA".equals(request.getTipoOperacion())) {
                return ejecutarVenta(userId, crypto, request.getCantidad(), precioActual);
            } else {
                System.err.println("❌ [TradingService] Tipo de operación inválido: " + request.getTipoOperacion());
                return new TradeResponse(false, "Tipo de operación no válido: " + request.getTipoOperacion());
            }

        } catch (InsufficientFundsExeption e) {
            System.err.println("❌ [TradingService] Fondos insuficientes: " + e.getMessage());
            return new TradeResponse(false, "Fondos insuficientes: " + e.getMessage());
        } catch (TradingExeption e) {
            System.err.println("❌ [TradingService] Error de trading: " + e.getMessage());
            return new TradeResponse(false, "Error de trading: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ [TradingService] Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return new TradeResponse(false, "Error interno: " + e.getMessage());
        }
    }

    /**
     * Ejecuta una compra de criptomoneda
     */
    private TradeResponse ejecutarCompra(Integer userId, Cryptocurrency crypto,
                                         BigDecimal cantidad, BigDecimal precioActual) {

        System.out.println("💰 [TradingService] Ejecutando COMPRA:");
        System.out.println("   - Usuario: " + userId);
        System.out.println("   - Cripto: " + crypto.getSimbolo() + " (" + crypto.getId() + ")");
        System.out.println("   - Cantidad: " + cantidad);
        System.out.println("   - Precio: " + precioActual);

        try {
            // Calcular costo total
            BigDecimal costoSinComision = cantidad.multiply(precioActual);
            BigDecimal comision = costoSinComision.multiply(COMISION_PORCENTAJE);
            BigDecimal costoTotal = costoSinComision.add(comision);

            System.out.println("   - Costo sin comisión: " + costoSinComision);
            System.out.println("   - Comisión: " + comision);
            System.out.println("   - Costo total: " + costoTotal);

            // Verificar y obtener portafolio
            Portafolio portafolio = obtenerOCrearPortafolio(userId);
            System.out.println("   - Saldo USD actual: " + portafolio.getSaldoUsd());

            if (portafolio.getSaldoUsd().compareTo(costoTotal) < 0) {
                String errorMsg = String.format("Saldo insuficiente. Necesario: $%.2f, Disponible: $%.2f",
                        costoTotal.doubleValue(), portafolio.getSaldoUsd().doubleValue());
                System.err.println("❌ [TradingService] " + errorMsg);
                throw new InsufficientFundsExeption(errorMsg);
            }

            // ✅ PASO 1: Crear orden de compra
            Orden ordenCompra = new Orden(userId, crypto.getId(), "compra", cantidad, precioActual);
            ordenCompra = ordenRepo.save(ordenCompra);
            System.out.println("✅ [TradingService] Orden de compra creada con ID: " + ordenCompra.getId());

            // ✅ PASO 2: Crear orden de venta ficticia (requerida por tu BD)
            Orden ordenVenta = new Orden(userId, crypto.getId(), "venta", cantidad, precioActual);
            ordenVenta = ordenRepo.save(ordenVenta);
            System.out.println("✅ [TradingService] Orden de venta ficticia creada con ID: " + ordenVenta.getId());

            // PASO 3: Actualizar saldo USD
            portafolio.setSaldoUsd(portafolio.getSaldoUsd().subtract(costoTotal));
            portafolio = portafolioRepo.save(portafolio);
            System.out.println("✅ [TradingService] Portafolio actualizado. Nuevo saldo: " + portafolio.getSaldoUsd());

            // PASO 4: Actualizar o crear wallet de cripto
            Wallet wallet = obtenerOCrearWallet(userId, crypto.getId());
            BigDecimal saldoAnterior = wallet.getSaldo();
            wallet.setSaldo(wallet.getSaldo().add(cantidad));
            wallet = walletRepo.save(wallet);
            System.out.println("✅ [TradingService] Wallet actualizada. Saldo anterior: " + saldoAnterior + ", Nuevo saldo: " + wallet.getSaldo());

            // ✅ PASO 5: Registrar transacción con IDs de órdenes reales
            Transaction transaction = new Transaction();
            transaction.setUserId(userId);
            transaction.setCryptoId(crypto.getId());
            transaction.setTipoTransaccion("COMPRA");
            transaction.setCantidad(cantidad);
            transaction.setPrecioEjecucion(precioActual);
            transaction.setComision(comision);
            transaction.setFechaEjecucion(new Timestamp(System.currentTimeMillis()));
            
            // ✅ CRÍTICO: Usar los IDs de las órdenes reales
            transaction.setOrdenCompraId(ordenCompra.getId());
            transaction.setOrdenVentaId(ordenVenta.getId());
            
            transaction = transactionRepo.save(transaction);
            System.out.println("✅ [TradingService] Transacción registrada con ID: " + transaction.getId());

            // Preparar respuesta
            TradeResponse response = new TradeResponse(true, "Compra ejecutada exitosamente");
            response.setCantidadEjecutada(cantidad);
            response.setPrecioEjecutado(precioActual);
            response.setComision(comision);
            response.setNuevoSaldoUsd(portafolio.getSaldoUsd());
            response.setNuevoSaldoCripto(wallet.getSaldo());

            System.out.println("✅ [TradingService] COMPRA EXITOSA completada");
            return response;

        } catch (Exception e) {
            System.err.println("❌ [TradingService] Error en ejecutarCompra: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-lanzar para que @Transactional maneje el rollback
        }
    }

    /**
     * Ejecuta una venta de criptomoneda
     */
    private TradeResponse ejecutarVenta(Integer userId, Cryptocurrency crypto,
                                        BigDecimal cantidad, BigDecimal precioActual) {

        System.out.println("💸 [TradingService] Ejecutando VENTA:");
        System.out.println("   - Usuario: " + userId);
        System.out.println("   - Cripto: " + crypto.getSimbolo() + " (" + crypto.getId() + ")");
        System.out.println("   - Cantidad: " + cantidad);
        System.out.println("   - Precio: " + precioActual);

        try {
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

            // ✅ PASO 1: Crear orden de venta
            Orden ordenVenta = new Orden(userId, crypto.getId(), "venta", cantidad, precioActual);
            ordenVenta = ordenRepo.save(ordenVenta);
            System.out.println("✅ [TradingService] Orden de venta creada con ID: " + ordenVenta.getId());

            // ✅ PASO 2: Crear orden de compra ficticia (requerida por tu BD)
            Orden ordenCompra = new Orden(userId, crypto.getId(), "compra", cantidad, precioActual);
            ordenCompra = ordenRepo.save(ordenCompra);
            System.out.println("✅ [TradingService] Orden de compra ficticia creada con ID: " + ordenCompra.getId());

            // PASO 3: Actualizar saldo cripto
            wallet.setSaldo(wallet.getSaldo().subtract(cantidad));
            wallet = walletRepo.save(wallet);
            System.out.println("✅ [TradingService] Wallet actualizada. Nuevo saldo: " + wallet.getSaldo());

            // PASO 4: Actualizar saldo USD
            Portafolio portafolio = obtenerOCrearPortafolio(userId);
            portafolio.setSaldoUsd(portafolio.getSaldoUsd().add(ingresoNeto));
            portafolio = portafolioRepo.save(portafolio);
            System.out.println("✅ [TradingService] Portafolio actualizado. Nuevo saldo: " + portafolio.getSaldoUsd());

            // ✅ PASO 5: Registrar transacción con IDs de órdenes reales
            Transaction transaction = new Transaction();
            transaction.setUserId(userId);
            transaction.setCryptoId(crypto.getId());
            transaction.setTipoTransaccion("VENTA");
            transaction.setCantidad(cantidad);
            transaction.setPrecioEjecucion(precioActual);
            transaction.setComision(comision);
            transaction.setFechaEjecucion(new Timestamp(System.currentTimeMillis()));
            
            // ✅ CRÍTICO: Usar los IDs de las órdenes reales
            transaction.setOrdenCompraId(ordenCompra.getId());
            transaction.setOrdenVentaId(ordenVenta.getId());
            
            transaction = transactionRepo.save(transaction);
            System.out.println("✅ [TradingService] Transacción registrada con ID: " + transaction.getId());

            // Preparar respuesta
            TradeResponse response = new TradeResponse(true, "Venta ejecutada exitosamente");
            response.setCantidadEjecutada(cantidad);
            response.setPrecioEjecutado(precioActual);
            response.setComision(comision);
            response.setNuevoSaldoUsd(portafolio.getSaldoUsd());
            response.setNuevoSaldoCripto(wallet.getSaldo());

            System.out.println("✅ [TradingService] VENTA EXITOSA completada");
            return response;

        } catch (Exception e) {
            System.err.println("❌ [TradingService] Error en ejecutarVenta: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Valida el request de trading
     */
    private void validarRequest(TradeRequest request) {
        System.out.println("🔍 [TradingService] Validando request...");
        
        if (request == null) {
            throw new TradingExeption("Request no puede ser null");
        }
        
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

        // Validación adicional: cantidad máxima razonable
        if (request.getCantidad().compareTo(new BigDecimal("1000000")) > 0) {
            throw new TradingExeption("Cantidad demasiado grande");
        }

        System.out.println("✅ [TradingService] Request validado correctamente");
    }

    /**
     * Obtiene o crea el portafolio de un usuario con mejor manejo de errores
     */
    private Portafolio obtenerOCrearPortafolio(Integer userId) {
        System.out.println("🔍 [TradingService] Obteniendo portafolio para userId: " + userId);
        
        try {
            Optional<Portafolio> portafolioOpt = portafolioRepo.findByUserId(userId);
            
            if (portafolioOpt.isPresent()) {
                System.out.println("✅ [TradingService] Portafolio encontrado");
                return portafolioOpt.get();
            } else {
                System.out.println("⚠️ [TradingService] Portafolio no encontrado, creando nuevo...");
                Portafolio nuevoPortafolio = new Portafolio(userId, new BigDecimal("10000.00"));
                Portafolio savedPortafolio = portafolioRepo.save(nuevoPortafolio);
                System.out.println("✅ [TradingService] Nuevo portafolio creado con ID: " + savedPortafolio.getId());
                return savedPortafolio;
            }
        } catch (Exception e) {
            System.err.println("❌ [TradingService] Error obteniendo/creando portafolio: " + e.getMessage());
            throw new TradingExeption("Error accediendo al portafolio: " + e.getMessage());
        }
    }

    /**
     * Obtiene o crea el wallet de cripto para un usuario con mejor manejo de errores
     */
    private Wallet obtenerOCrearWallet(Integer userId, Integer cryptoId) {
        System.out.println("🔍 [TradingService] Obteniendo wallet para userId: " + userId + ", cryptoId: " + cryptoId);
        
        try {
            Optional<Wallet> walletOpt = walletRepo.findByUserIdAndCryptoId(userId, cryptoId);
            
            if (walletOpt.isPresent()) {
                System.out.println("✅ [TradingService] Wallet encontrada");
                return walletOpt.get();
            } else {
                System.out.println("⚠️ [TradingService] Wallet no encontrada, creando nueva...");
                Wallet nuevoWallet = new Wallet(userId, cryptoId, BigDecimal.ZERO);
                Wallet savedWallet = walletRepo.save(nuevoWallet);
                System.out.println("✅ [TradingService] Nueva wallet creada con ID: " + savedWallet.getId());
                return savedWallet;
            }
        } catch (Exception e) {
            System.err.println("❌ [TradingService] Error obteniendo/creando wallet: " + e.getMessage());
            throw new TradingExeption("Error accediendo al wallet: " + e.getMessage());
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