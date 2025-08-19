package com.trading.cripto.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "ordenes")
public class Orden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Integer id;

    @Column(name = "id_usuario", nullable = false)
    private Integer userId;

    @Column(name = "id_cripto", nullable = false)
    private Integer cryptoId;

    @Column(name = "tipo_orden", nullable = false)
    private String tipoOrden; // "compra" o "venta"

    @Column(name = "cantidad", precision = 20, scale = 8, nullable = false)
    private BigDecimal cantidad;

    @Column(name = "precio_por_unidad", precision = 20, scale = 8, nullable = false)
    private BigDecimal precioPorUnidad;

    @Column(name = "estado")
    private String estado = "ejecutada"; // Para nuestro caso siempre será ejecutada

    @Column(name = "fecha_creacion")
    private Timestamp fechaCreacion;

    // Constructores
    public Orden() {
        this.fechaCreacion = new Timestamp(System.currentTimeMillis());
    }

    public Orden(Integer userId, Integer cryptoId, String tipoOrden, 
                 BigDecimal cantidad, BigDecimal precioPorUnidad) {
        this();
        this.userId = userId;
        this.cryptoId = cryptoId;
        this.tipoOrden = tipoOrden;
        this.cantidad = cantidad;
        this.precioPorUnidad = precioPorUnidad;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getCryptoId() { return cryptoId; }
    public void setCryptoId(Integer cryptoId) { this.cryptoId = cryptoId; }

    public String getTipoOrden() { return tipoOrden; }
    public void setTipoOrden(String tipoOrden) { this.tipoOrden = tipoOrden; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioPorUnidad() { return precioPorUnidad; }
    public void setPrecioPorUnidad(BigDecimal precioPorUnidad) { this.precioPorUnidad = precioPorUnidad; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
