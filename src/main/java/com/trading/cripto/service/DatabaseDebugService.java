package com.trading.cripto.service;

import com.trading.cripto.model.Cryptocurrency;
import com.trading.cripto.model.PriceHistory;
import com.trading.cripto.repository.CryptocurrencyRepository;
import com.trading.cripto.repository.PriceHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para debug y verificación de la base de datos
 */
@Service
public class DatabaseDebugService {

    @Autowired
    private CryptocurrencyRepository cryptoRepo;

    @Autowired
    private PriceHistoryRepository priceHistoryRepo;

    /**
     * Verifica si los datos se están guardando correctamente
     */
    @Transactional
    public void testDatabaseInsert() {
        System.out.println("🧪 INICIANDO TEST DE BASE DE DATOS");
        System.out.println("===================================");

        try {
            // 1. Verificar criptomonedas existentes
            List<Cryptocurrency> cryptos = cryptoRepo.findAll();
            System.out.println("📊 Criptomonedas en BD: " + cryptos.size());
            
            for (Cryptocurrency crypto : cryptos) {
                System.out.println(String.format("   %s (%s): $%.4f", 
                    crypto.getNombre(), 
                    crypto.getSimbolo(), 
                    crypto.getPrecio() != null ? crypto.getPrecio() : BigDecimal.ZERO));
            }

            // 2. Insertar un registro de prueba en price_history
            if (!cryptos.isEmpty()) {
                Cryptocurrency testCrypto = cryptos.get(0);
                
                System.out.println("\n💾 Insertando registro de prueba en price_history...");
                
                PriceHistory testPrice = new PriceHistory();
                testPrice.setCryptoId(testCrypto.getId());
                testPrice.setPrecio(new BigDecimal("99.99"));
                testPrice.setVolumen(new BigDecimal("1000"));
                testPrice.setTimestamp(LocalDateTime.now());
                testPrice.setIntervalo("TEST");
                
                PriceHistory saved = priceHistoryRepo.save(testPrice);
                
                System.out.println("✅ Registro guardado con ID: " + saved.getId());
                System.out.println("   Crypto ID: " + saved.getCryptoId());
                System.out.println("   Precio: $" + saved.getPrecio());
                System.out.println("   Timestamp: " + saved.getTimestamp());
                
                // 3. Verificar que se guardó
                System.out.println("\n🔍 Verificando registro guardado...");
                List<PriceHistory> recentPrices = priceHistoryRepo
                    .findLastNPrices(testCrypto.getId(), 1);
                
                if (!recentPrices.isEmpty()) {
                    PriceHistory latest = recentPrices.get(0);
                    System.out.println("✅ Último registro encontrado:");
                    System.out.println("   ID: " + latest.getId());
                    System.out.println("   Precio: $" + latest.getPrecio());
                    System.out.println("   Timestamp: " + latest.getTimestamp());
                } else {
                    System.out.println("❌ No se encontró el registro guardado");
                }
            }

            // 4. Estadísticas generales
            System.out.println("\n📈 Estadísticas de price_history:");
            long totalRecords = priceHistoryRepo.count();
            System.out.println("   Total de registros: " + totalRecords);
            
            if (totalRecords > 0) {
                // Mostrar últimos 5 registros
                System.out.println("\n📋 Últimos 5 registros:");
                List<PriceHistory> recent = priceHistoryRepo
                    .findAll()
                    .stream()
                    .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                    .limit(5)
                    .toList();
                
                for (PriceHistory price : recent) {
                    System.out.println(String.format("   ID:%d | Crypto:%d | $%.4f | %s", 
                        price.getId(),
                        price.getCryptoId(),
                        price.getPrecio(),
                        price.getTimestamp()));
                }
            }

        } catch (Exception e) {
            System.err.println("❌ ERROR EN TEST DE BD: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n🧪 TEST DE BASE DE DATOS COMPLETADO");
        System.out.println("=====================================");
    }

    /**
     * Actualiza precios manualmente para testing
     */
    @Transactional
    public void updateCryptoPricesManually() {
        System.out.println("🔄 Actualizando precios manualmente...");
        
        List<Cryptocurrency> cryptos = cryptoRepo.findAll();
        
        for (Cryptocurrency crypto : cryptos) {
            // Generar precio aleatorio
            BigDecimal newPrice = new BigDecimal(Math.random() * 100 + 10)
                .setScale(4, BigDecimal.ROUND_HALF_UP);
            
            crypto.setPrecio(newPrice);
            cryptoRepo.save(crypto);
            
            // Guardar en histórico
            PriceHistory history = new PriceHistory();
            history.setCryptoId(crypto.getId());
            history.setPrecio(newPrice);
            history.setVolumen(new BigDecimal(Math.random() * 10000 + 1000));
            history.setTimestamp(LocalDateTime.now());
            history.setIntervalo("MANUAL");
            
            priceHistoryRepo.save(history);
            
            System.out.println(String.format("✅ %s actualizado a $%.4f", 
                crypto.getSimbolo(), newPrice));
        }
    }

    /**
     * Limpia registros de prueba
     */
    @Transactional
    public void cleanTestData() {
        System.out.println("🧹 Limpiando datos de prueba...");
        
        // Eliminar registros con intervalo "TEST" o "MANUAL"
        List<PriceHistory> testRecords = priceHistoryRepo.findAll()
            .stream()
            .filter(p -> "TEST".equals(p.getIntervalo()) || "MANUAL".equals(p.getIntervalo()))
            .toList();
        
        priceHistoryRepo.deleteAll(testRecords);
        
        System.out.println("✅ " + testRecords.size() + " registros de prueba eliminados");
    }
}
