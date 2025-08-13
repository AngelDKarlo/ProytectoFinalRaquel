package com.trading.cripto.service;

import com.trading.cripto.model.PriceTick;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CsvImportService {

    public List<PriceTick> importData(String resourcePath) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                this.getClass().getResourceAsStream(resourcePath)))) {

            // Omitir la cabecera
            return reader.lines().skip(1).map(line -> {
                String[] data = line.split(",");
                // Asumiendo formato: timestamp,open,high,low,close,...
                // Usaremos 'close' como el precio del tick
                LocalDateTime timestamp = LocalDateTime.parse(data[0], DateTimeFormatter.ISO_DATE_TIME);
                BigDecimal price = new BigDecimal(data[4]);
                return new PriceTick(timestamp, price);
            }).collect(Collectors.toList());

        } catch (Exception e) {
            // Manejar la excepción adecuadamente
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}