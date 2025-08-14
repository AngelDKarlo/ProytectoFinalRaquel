package com.trading.cripto.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history")
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "crypto_id", nullable = false)
    private Integer cryptoId;

    @Column(name = "precio", precision = 20, scale = 8, nullable = false)
    private BigDecimal precio;

    @Column(name = "volumen", precision = 20, scale = 8)
    private BigDecimal volumen;

    @Column(name = "precio_apertura", precision = 20, scale = 8)
    private BigDecimal precioApertura;

    @Column(name = "precio_maximo", precision = 20, scale = 8)
    private BigDecimal precioMaximo;

    @Column(name = "precio_minimo", precision = 20, scale = 8)
    private BigDecimal precioMinimo;

    @Column(name = "precio_cierre", precision = 20, scale = 8)
    private BigDecimal precioCierre;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "intervalo")
    private String intervalo; // "1m", "5m", "1h", "1d"

    // Constructores
    public PriceHistory() {
        this.timestamp = LocalDateTime.now();
    }

    public PriceHistory(Integer cryptoId, BigDecimal precio) {
        this.cryptoId = cryptoId;
        this.precio = precio;
        this.timestamp = LocalDateTime.now();
        this.intervalo = "1m";
    }

    public PriceHistory(Integer cryptoId, BigDecimal precio, BigDecimal volumen,
                        BigDecimal precioApertura, BigDecimal precioMaximo,
                        BigDecimal precioMinimo, BigDecimal precioCierre,
                        LocalDateTime timestamp, String intervalo) {
        this.cryptoId = cryptoId;
        this.precio = precio;
        this.volumen = volumen;
        this.precioApertura = precioApertura;
        this.precioMaximo = precioMaximo;
        this.precioMinimo = precioMinimo;
        this.precioCierre = precioCierre;
        this.timestamp = timestamp;
        this.intervalo = intervalo;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCryptoId() { return cryptoId; }
    public void setCryptoId(Integer cryptoId) { this.cryptoId = cryptoId; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public BigDecimal getVolumen() { return volumen; }
    public void setVolumen(BigDecimal volumen) { this.volumen = volumen; }

    public BigDecimal getPrecioApertura() { return precioApertura; }
    public void setPrecioApertura(BigDecimal precioApertura) { this.precioApertura = precioApertura; }

    public BigDecimal getPrecioMaximo() { return precioMaximo; }
    public void setPrecioMaximo(BigDecimal precioMaximo) { this.precioMaximo = precioMaximo; }

    public BigDecimal getPrecioMinimo() { return precioMinimo; }
    public void setPrecioMinimo(BigDecimal precioMinimo) { this.precioMinimo = precioMinimo; }

    public BigDecimal getPrecioCierre() { return precioCierre; }
    public void setPrecioCierre(BigDecimal precioCierre) { this.precioCierre = precioCierre; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getIntervalo() { return intervalo; }
    public void setIntervalo(String intervalo) { this.intervalo = intervalo; }
}