package com.trading.cripto.service;

import com.trading.cripto.model.PriceTick;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para importar datos desde archivos CSV
 */
@Service
public class CsvImportService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Importa datos de precios desde un archivo CSV
     * @param path Ruta del archivo CSV en resources
     * @return Lista de PriceTick con los datos importados
     */
    public List<PriceTick> importData(String path) {
        List<PriceTick> data = new ArrayList<>();

        try {
            // Quitar la barra inicial si existe
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            ClassPathResource resource = new ClassPathResource(path);

            if (!resource.exists()) {
                System.out.println("⚠️  Archivo CSV no encontrado: " + path);
                return data; // Retornar lista vacía
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream()))) {

                String line;
                boolean isFirstLine = true;

                while ((line = reader.readLine()) != null) {
                    // Saltar la línea de encabezados
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    try {
                        PriceTick tick = parseLine(line);
                        if (tick != null) {
                            data.add(tick);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parseando línea: " + e.getMessage());
                    }
                }

                System.out.println("✅ Importados " + data.size() +
                        " registros desde " + path);
            }

        } catch (IOException e) {
            System.err.println("❌ Error leyendo archivo CSV: " + e.getMessage());
        }

        return data;
    }

    /**
     * Parsea una línea del CSV y crea un PriceTick
     * Formato esperado: timestamp,open,high,low,close,volume
     */
    private PriceTick parseLine(String line) {
        try {
            String[] parts = line.split(",");
            if (parts.length < 5) {
                return null;
            }

            // Parsear timestamp
            LocalDateTime timestamp = LocalDateTime.parse(parts[0].trim(), formatter);

            // Usar el precio de cierre (close) como precio principal
            BigDecimal price = new BigDecimal(parts[4].trim());

            return new PriceTick(timestamp, price);

        } catch (Exception e) {
            System.err.println("Error parseando línea CSV: " + line);
            return null;
        }
    }

    /**
     * Importa y valida datos de múltiples archivos CSV
     * @param symbols Lista de símbolos de criptomonedas
     * @return true si todos los archivos se importaron correctamente
     */
    public boolean importAllData(List<String> symbols) {
        boolean allSuccess = true;

        for (String symbol : symbols) {
            String path = "historical_data/" + symbol + "_USD_historical.csv";
            List<PriceTick> data = importData(path);

            if (data.isEmpty()) {
                System.out.println("⚠️  No se pudieron importar datos para " + symbol);
                allSuccess = false;
            }
        }

        return allSuccess;
    }
}