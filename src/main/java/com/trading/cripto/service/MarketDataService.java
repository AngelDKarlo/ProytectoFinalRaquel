package com.trading.cripto.service;

import com.trading.cripto.model.PriceTick;
import com.trading.cripto.repository.CryptocurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MarketDataService {

    @Autowired
    private CsvImportService csvImportService;

    @Autowired
    private CryptocurrencyRepository cryptoRepo;

    // Almacén para los datos históricos en memoria
    private final Map<String, List<PriceTick>> historicalData = new ConcurrentHashMap<>();
    // Índice para saber en qué punto de la simulación vamos para cada moneda
    private final Map<String, AtomicInteger> currentTickIndex = new ConcurrentHashMap<>();

    // Este método se ejecuta después de que el bean ha sido creado
    @PostConstruct
    public void loadData() {
        System.out.println("Cargando datos históricos del mercado...");
        // Carga los datos para cada criptomoneda que tienes
        loadCurrencyData("ZOR", "/historical_data/ZOR_USD_historical.csv");
        loadCurrencyData("NEB", "/historical_data/NEB_USD_historical.csv");
        loadCurrencyData("LUM", "/historical_data/LUM_USD_historical.csv");
        System.out.println("Datos históricos cargados.");
    }

    private void loadCurrencyData(String symbol, String path) {
        historicalData.put(symbol, csvImportService.importData(path));
        currentTickIndex.put(symbol, new AtomicInteger(0));
    }

    // Tarea programada que se ejecuta cada 5 segundos.
    // fixedRate=5000 significa 5000 milisegundos.
    @Scheduled(fixedRate = 5000)
    public void publishNextPriceTick() {
        historicalData.forEach((symbol, prices) -> {
            int index = currentTickIndex.get(symbol).getAndIncrement();

            // Si llegamos al final de los datos, reiniciamos la simulación
            if (index >= prices.size()) {
                index = 0;
                currentTickIndex.get(symbol).set(0);
            }

            PriceTick currentTick = prices.get(index);
            System.out.println("Nuevo Precio para " + symbol + ": " + currentTick.getPrice());

            // Actualiza el precio en la base de datos
            cryptoRepo.findBySimbolo(symbol).ifPresent(crypto -> {
                crypto.setPrecio(currentTick.getPrice());
                cryptoRepo.save(crypto);
            });

        });
    }
}