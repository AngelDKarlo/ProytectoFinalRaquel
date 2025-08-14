package com.trading.cripto.dto;

import java.math.BigDecimal;

public class TradeRequest {
    private String symboloCripto; // "ZOR", "NEB", "LUM"
    private String tipoOperacion; // "COMPRA" o "VENTA"
    private BigDecimal cantidad;
    private BigDecimal precioMaximo; // Para compras (opcional)
    private BigDecimal precioMinimo; // Para ventas (opcional)

    // Constructores
    public TradeRequest() {}

    // Getters y Setters
    public String getSymboloCripto() { return symboloCripto; }
    public void setSymboloCripto(String symboloCripto) { this.symboloCripto = symboloCripto; }

    public String getTipoOperacion() { return tipoOperacion; }
    public void setTipoOperacion(String tipoOperacion) { this.tipoOperacion = tipoOperacion; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioMaximo() { return precioMaximo; }
    public void setPrecioMaximo(BigDecimal precioMaximo) { this.precioMaximo = precioMaximo; }

    public BigDecimal getPrecioMinimo() { return precioMinimo; }
    public void setPrecioMinimo(BigDecimal precioMinimo) { this.precioMinimo = precioMinimo; }
}
