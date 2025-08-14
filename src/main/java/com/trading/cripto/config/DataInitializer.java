package com.trading.cripto.config;

import com.trading.cripto.util.CsvDataGenerator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Inicializador de datos que se ejecuta al arrancar la aplicación
 * Genera archivos CSV si no existen
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData() {
        return args -> {
            System.out.println("🔍 Verificando datos históricos...");

            // Verificar si los archivos CSV existen
            String[] requiredFiles = {
                    "historical_data/ZOR_USD_historical.csv",
                    "historical_data/NEB_USD_historical.csv",
                    "historical_data/LUM_USD_historical.csv"
            };

            boolean allFilesExist = true;
            for (String file : requiredFiles) {
                try {
                    ClassPathResource resource = new ClassPathResource(file);
                    if (!resource.exists()) {
                        System.out.println("⚠️  Archivo no encontrado: " + file);
                        allFilesExist = false;
                    } else {
                        // Verificar si el archivo tiene suficientes datos
                        File csvFile = resource.getFile();
                        long lineCount = Files.lines(csvFile.toPath()).count();
                        if (lineCount < 100) { // Menos de 100 líneas
                            System.out.println("⚠️  Archivo con pocos datos: " + file +
                                    " (" + lineCount + " líneas)");
                            allFilesExist = false;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("⚠️  No se pudo verificar: " + file);
                    allFilesExist = false;
                }
            }

            // Si no existen todos los archivos o tienen pocos datos, generarlos
            if (!allFilesExist) {
                System.out.println("\n📊 Generando datos históricos faltantes...");

                try {
                    // Crear directorio si no existe
                    Path historicalDataPath = Paths.get("src/main/resources/historical_data");
                    if (!Files.exists(historicalDataPath)) {
                        Files.createDirectories(historicalDataPath);
                        System.out.println("📁 Directorio creado: historical_data/");
                    }

                    // Generar todos los archivos CSV
                    CsvDataGenerator.generateAllCsvFiles();

                    System.out.println("✅ Datos históricos generados exitosamente");
                } catch (Exception e) {
                    System.err.println("❌ Error generando datos: " + e.getMessage());
                    System.out.println("⚠️  El sistema funcionará con datos sintéticos en memoria");
                }
            } else {
                System.out.println("✅ Todos los archivos de datos históricos están presentes");
            }

            System.out.println("🚀 Sistema de datos listo\n");
        };
    }
}