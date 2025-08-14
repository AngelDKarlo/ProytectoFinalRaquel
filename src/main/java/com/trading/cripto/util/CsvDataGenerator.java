package com.trading.cripto.util;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generador de datos históricos CSV para las criptomonedas
 * Crea archivos CSV con datos realistas de precios
 */
public class CsvDataGenerator {

    private static final Random random = new Random();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Clase para representar un punto de datos OHLCV
     */
    static class PriceData {
        LocalDateTime timestamp;
        BigDecimal open;
        BigDecimal high;
        BigDecimal low;
        BigDecimal close;
        int volume;

        public PriceData(LocalDateTime timestamp, BigDecimal open, BigDecimal high,
                         BigDecimal low, BigDecimal close, int volume) {
            this.timestamp = timestamp;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.volume = volume;
        }

        public String toCsv() {
            return String.format("%s,%.4f,%.4f,%.4f,%.4f,%d",
                    timestamp.format(formatter),
                    open, high, low, close, volume);
        }
    }

    /**
     * Genera datos de precios históricos para una criptomoneda
     */
    public static List<PriceData> generatePriceData(String symbol, BigDecimal basePrice,
                                                    double volatility, int numDays) {
        List<PriceData> data = new ArrayList<>();
        LocalDateTime currentTime = LocalDateTime.now().minusDays(numDays);
        BigDecimal currentPrice = basePrice;

        // Generar datos cada 5 minutos
        int intervalsPerDay = 288; // 24 horas * 12 (cada 5 minutos)
        int totalIntervals = numDays * intervalsPerDay;

        // Parámetros para tendencias
        double trend = (random.nextDouble() - 0.5) * 0.001; // Tendencia ligera
        int cycleLength = 48 + random.nextInt(96); // Ciclos de 4-12 horas

        for (int i = 0; i < totalIntervals; i++) {
            // Efecto cíclico (simula patrones diarios)
            double cycleEffect = Math.sin(2 * Math.PI * i / cycleLength) * volatility * 0.5;

            // Random walk con reversión a la media
            double randomChange = randomGaussian() * volatility;
            double meanReversion = basePrice.subtract(currentPrice)
                    .divide(basePrice, 8, RoundingMode.HALF_UP)
                    .doubleValue() * 0.001;

            // Calcular cambio de precio
            double priceChange = trend + cycleEffect + randomChange + meanReversion;
            currentPrice = currentPrice.multiply(BigDecimal.valueOf(1 + priceChange));

            // Prevenir precios negativos o muy bajos
            BigDecimal minPrice = basePrice.multiply(BigDecimal.valueOf(0.1));
            if (currentPrice.compareTo(minPrice) < 0) {
                currentPrice = minPrice;
            }

            // Generar OHLC
            BigDecimal open = currentPrice.multiply(
                    BigDecimal.valueOf(1 + randomGaussian() * volatility * 0.1));
            BigDecimal high = open.max(currentPrice).multiply(
                    BigDecimal.valueOf(1 + Math.abs(randomGaussian()) * volatility * 0.2));
            BigDecimal low = open.min(currentPrice).multiply(
                    BigDecimal.valueOf(1 - Math.abs(randomGaussian()) * volatility * 0.2));
            BigDecimal close = currentPrice;

            // Volumen (distribución log-normal)
            int volume = (int) (Math.exp(randomGaussian() * 0.5 + 10) * 1000);

            // Redondear valores
            open = open.setScale(4, RoundingMode.HALF_UP);
            high = high.setScale(4, RoundingMode.HALF_UP);
            low = low.setScale(4, RoundingMode.HALF_UP);
            close = close.setScale(4, RoundingMode.HALF_UP);

            data.add(new PriceData(currentTime, open, high, low, close, volume));

            // Avanzar tiempo
            currentTime = currentTime.plusMinutes(5);

            // Eventos de alta volatilidad ocasionales (1% probabilidad)
            if (random.nextDouble() < 0.01) {
                double spike = 0.95 + random.nextDouble() * 0.1; // ±5%
                currentPrice = currentPrice.multiply(BigDecimal.valueOf(spike));
            }
        }

        return data;
    }

    /**
     * Guarda los datos en un archivo CSV
     */
    public static void saveToCsv(String filename, List<PriceData> data) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            // Escribir encabezados
            writer.write("timestamp,open,high,low,close,volume\n");

            // Escribir datos
            for (PriceData price : data) {
                writer.write(price.toCsv() + "\n");
            }

            System.out.println("✅ Archivo generado: " + filename + " (" + data.size() + " registros)");
        }
    }

    /**
     * Genera distribución gaussiana (normal)
     */
    private static double randomGaussian() {
        return random.nextGaussian();
    }

    /**
     * Método principal para generar todos los archivos CSV
     */
    public static void generateAllCsvFiles() {
        System.out.println("🚀 Generando datos históricos de criptomonedas en Java...\n");

        // Crear directorio si no existe
        String outputDir = "src/main/resources/historical_data";
        try {
            Path path = Paths.get(outputDir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                System.out.println("📁 Directorio creado: " + outputDir);
            }
        } catch (IOException e) {
            System.err.println("Error creando directorio: " + e.getMessage());
        }

        // Configuración para cada criptomoneda
        CryptoConfig[] cryptos = {
                new CryptoConfig("ZOR", new BigDecimal("12.58"), 0.05,
                        "Zorcoin - Pagos rápidos y seguros"),
                new CryptoConfig("NEB", new BigDecimal("8.34"), 0.08,
                        "Nebulium - Contratos inteligentes"),
                new CryptoConfig("LUM", new BigDecimal("45.67"), 0.03,
                        "Lumera - Interoperabilidad")
        };

        for (CryptoConfig crypto : cryptos) {
            System.out.println("📊 Generando datos para " + crypto.symbol +
                    " - " + crypto.description);
            System.out.println("   Precio base: $" + crypto.basePrice);
            System.out.println("   Volatilidad: " + (crypto.volatility * 100) + "%");

            // Generar 30 días de datos
            List<PriceData> data = generatePriceData(
                    crypto.symbol,
                    crypto.basePrice,
                    crypto.volatility,
                    30
            );

            // Guardar en archivo CSV
            String filename = outputDir + "/" + crypto.symbol + "_USD_historical.csv";
            try {
                saveToCsv(filename, data);
            } catch (IOException e) {
                System.err.println("❌ Error guardando " + filename + ": " + e.getMessage());
            }
            System.out.println();
        }

        System.out.println("✨ ¡Generación completada!");
        System.out.println("📁 Archivos guardados en: " + outputDir);
    }

    /**
     * Clase auxiliar para configuración de criptomonedas
     */
    static class CryptoConfig {
        String symbol;
        BigDecimal basePrice;
        double volatility;
        String description;

        public CryptoConfig(String symbol, BigDecimal basePrice,
                            double volatility, String description) {
            this.symbol = symbol;
            this.basePrice = basePrice;
            this.volatility = volatility;
            this.description = description;
        }
    }

    /**
     * Método main para ejecutar independientemente
     */
    public static void main(String[] args) {
        generateAllCsvFiles();
    }
}