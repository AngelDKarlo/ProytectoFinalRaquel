package com.trading.cripto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class TradeRequest {
    
    @NotBlank(message = "Símbolo de criptomoneda requerido")
    private String symboloCripto;
    
    @NotBlank(message = "Tipo de operación requerido")
    private String tipoOperacion; // "COMPRA" o "VENTA"
    
    @NotNull(message = "Cantidad requerida")
    @Positive(message = "La cantidad debe ser positiva")
    private BigDecimal cantidad;
    private BigDecimal precioMaximo; // Para compras (opcional)
    private BigDecimal precioMinimo; // Para ventas (opcional)

    // Constructores
    public TradeRequest() {}
    
    public TradeRequest(String symboloCripto, String tipoOperacion, BigDecimal cantidad) {
        this.symboloCripto = symboloCripto;
        this.tipoOperacion = tipoOperacion;
        this.cantidad = cantidad;
    }

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

    @Override
    public String toString() {
        return String.format("TradeRequest{symbol='%s', tipo='%s', cantidad=%s}", 
            symboloCripto, tipoOperacion, cantidad);
    }
}
