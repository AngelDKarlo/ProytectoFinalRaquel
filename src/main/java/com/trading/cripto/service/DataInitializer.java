package com.trading.cripto.service;

import com.trading.cripto.model.Cryptocurrency;
import com.trading.cripto.repository.CryptocurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private CryptocurrencyRepository cryptocurrencyRepository;

    @Override
    public void run(String... args) throws Exception {
        if (cryptocurrencyRepository.count() == 0) {

            Cryptocurrency zor = new Cryptocurrency();
            zor.setNombre("Zorcoin");
            zor.setSimbolo("ZOR");
            zor.setPrecio(BigDecimal.ZERO); // El precio se actualizará en tiempo real
            zor.setDescripcion("Un activo digital diseñado para pagos rápidos y seguros.");
            cryptocurrencyRepository.save(zor);

            Cryptocurrency neb = new Cryptocurrency();
            neb.setNombre("Nebulium");
            neb.setSimbolo("NEB");
            neb.setPrecio(BigDecimal.ZERO);
            neb.setDescripcion("Plataforma blockchain enfocada en contratos inteligentes escalables.");
            cryptocurrencyRepository.save(neb);

            Cryptocurrency lum = new Cryptocurrency();
            lum.setNombre("Lumera");
            lum.setSimbolo("LUM");
            lum.setPrecio(BigDecimal.ZERO);
            lum.setDescripcion("Red descentralizada orientada a la interoperabilidad entre cadenas.");
            cryptocurrencyRepository.save(lum);

        }
    }
}
