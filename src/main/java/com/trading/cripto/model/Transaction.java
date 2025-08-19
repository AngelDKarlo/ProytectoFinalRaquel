package com.trading.cripto.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "transacciones_ejecutadas")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaccion")
    private Integer id;

    @Column(name = "id_orden_compra")
    private Integer ordenCompraId;

    @Column(name = "id_orden_venta")
    private Integer ordenVentaId;

    @Column(name = "id_cripto", nullable = false)
    private Integer cryptoId;

    @Column(name = "cantidad", precision = 20, scale = 8, nullable = false)
    private BigDecimal cantidad;

    @Column(name = "precio_ejecucion", precision = 20, scale = 8, nullable = false)
    private BigDecimal precioEjecucion;

    @Column(name = "fecha_ejecucion")
    private Timestamp fechaEjecucion;

    @Column(name = "comision", precision = 10, scale = 8)
    private BigDecimal comision = BigDecimal.ZERO;

    @Column(name = "id_usuario", nullable = false)
    private Integer userId;

    @Column(name = "tipo_transaccion", nullable = false)
    private String tipoTransaccion;

    // Constructores
    public Transaction() {}

    // ✅ Constructor simplificado - no asignar IDs automáticamente
    public Transaction(Integer userId, Integer cryptoId, String tipoTransaccion,
                       BigDecimal cantidad, BigDecimal precioEjecucion) {
        this.userId = userId;
        this.cryptoId = cryptoId;
        this.tipoTransaccion = tipoTransaccion;
        this.cantidad = cantidad;
        this.precioEjecucion = precioEjecucion;
        this.fechaEjecucion = new Timestamp(System.currentTimeMillis());
        // ❌ NO asignar ordenCompraId y ordenVentaId aquí
        // Se asignarán después de crear las órdenes reales
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getOrdenCompraId() { return ordenCompraId; }
    public void setOrdenCompraId(Integer ordenCompraId) { this.ordenCompraId = ordenCompraId; }

    public Integer getOrdenVentaId() { return ordenVentaId; }
    public void setOrdenVentaId(Integer ordenVentaId) { this.ordenVentaId = ordenVentaId; }

    public Integer getCryptoId() { return cryptoId; }
    public void setCryptoId(Integer cryptoId) { this.cryptoId = cryptoId; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioEjecucion() { return precioEjecucion; }
    public void setPrecioEjecucion(BigDecimal precioEjecucion) { this.precioEjecucion = precioEjecucion; }

    public Timestamp getFechaEjecucion() { return fechaEjecucion; }
    public void setFechaEjecucion(Timestamp fechaEjecucion) { this.fechaEjecucion = fechaEjecucion; }

    public BigDecimal getComision() { return comision; }
    public void setComision(BigDecimal comision) { this.comision = comision; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getTipoTransaccion() { return tipoTransaccion; }
    public void setTipoTransaccion(String tipoTransaccion) { this.tipoTransaccion = tipoTransaccion; }
}