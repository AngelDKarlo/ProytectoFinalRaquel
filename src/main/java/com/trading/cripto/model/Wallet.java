package com.trading.cripto.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "wallets")  // ✅ Usar tu tabla existente
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_billetera")  // ✅ Usar tu campo existente
    private Integer id;

    @Column(name = "id_usuario", nullable = false)
    private Integer userId;

    @Column(name = "id_cripto", nullable = false)
    private Integer cryptoId;

    @Column(name = "saldo", precision = 20, scale = 8, nullable = false)
    private BigDecimal saldo = BigDecimal.ZERO;

    // Constructores
    public Wallet() {}

    public Wallet(Integer userId, Integer cryptoId, BigDecimal saldo) {
        this.userId = userId;
        this.cryptoId = cryptoId;
        this.saldo = saldo;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getCryptoId() { return cryptoId; }
    public void setCryptoId(Integer cryptoId) { this.cryptoId = cryptoId; }

    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
}