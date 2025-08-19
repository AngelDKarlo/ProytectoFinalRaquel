package com.trading.cripto.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "wallets_cripto")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_wallet")
    private Integer id;

    @Column(name = "id_usuario", nullable = false)
    private Integer userId;

    @Column(name = "id_cripto", nullable = false)
    private Integer cryptoId;

    @Column(name = "saldo", precision = 20, scale = 8, nullable = false)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(name = "fecha_creacion")
    private Timestamp fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private Timestamp fechaActualizacion;

    // Constructores
    public Wallet() {
        this.fechaCreacion = new Timestamp(System.currentTimeMillis());
        this.fechaActualizacion = new Timestamp(System.currentTimeMillis());
    }

    public Wallet(Integer userId, Integer cryptoId, BigDecimal saldo) {
        this();
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
    public void setSaldo(BigDecimal saldo) { 
        this.saldo = saldo; 
        this.fechaActualizacion = new Timestamp(System.currentTimeMillis());
    }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Timestamp getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Timestamp fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}