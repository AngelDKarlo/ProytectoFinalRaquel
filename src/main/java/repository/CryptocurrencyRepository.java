package repository;

import model.Cryptocurrency;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CryptocurrencyRepository extends JpaRepository<Cryptocurrency, Integer> {
    Optional<Cryptocurrency> findBySimbolo(String simbolo);
}
