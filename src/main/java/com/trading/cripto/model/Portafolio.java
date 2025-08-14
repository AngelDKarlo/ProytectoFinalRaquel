package com.trading.cripto.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "portafolio_usuario")
public class Portafolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_usuario", nullable = false, unique = true)
    private Integer userId;

    @Column(name = "saldo_usd", precision = 20, scale = 2, nullable = false)
    private BigDecimal saldoUsd = new BigDecimal("10000.00");

    @Column(name = "fecha_creacion")
    private Timestamp fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private Timestamp fechaActualizacion;

    // Constructores
    public Portafolio() {}

    public Portafolio(Integer userId, BigDecimal saldoUsd) {
        this.userId = userId;
        this.saldoUsd = saldoUsd;
        this.fechaCreacion = new Timestamp(System.currentTimeMillis());
        this.fechaActualizacion = new Timestamp(System.currentTimeMillis());
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public BigDecimal getSaldoUsd() { return saldoUsd; }
    public void setSaldoUsd(BigDecimal saldoUsd) {
        this.saldoUsd = saldoUsd;
        this.fechaActualizacion = new Timestamp(System.currentTimeMillis());
    }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Timestamp getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Timestamp fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
