package com.trading.cripto.dto;

import java.math.BigDecimal;

public class TradeResponse {
    private boolean exitoso;
    private String mensaje;
    private BigDecimal cantidadEjecutada;
    private BigDecimal precioEjecutado;
    private BigDecimal comision;
    private BigDecimal nuevoSaldoUsd;
    private BigDecimal nuevoSaldoCripto;

    // Constructores
    public TradeResponse() {}

    public TradeResponse(boolean exitoso, String mensaje) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
    }

    // Getters y Setters
    public boolean isExitoso() { return exitoso; }
    public void setExitoso(boolean exitoso) { this.exitoso = exitoso; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public BigDecimal getCantidadEjecutada() { return cantidadEjecutada; }
    public void setCantidadEjecutada(BigDecimal cantidadEjecutada) { this.cantidadEjecutada = cantidadEjecutada; }

    public BigDecimal getPrecioEjecutado() { return precioEjecutado; }
    public void setPrecioEjecutado(BigDecimal precioEjecutado) { this.precioEjecutado = precioEjecutado; }

    public BigDecimal getComision() { return comision; }
    public void setComision(BigDecimal comision) { this.comision = comision; }

    public BigDecimal getNuevoSaldoUsd() { return nuevoSaldoUsd; }
    public void setNuevoSaldoUsd(BigDecimal nuevoSaldoUsd) { this.nuevoSaldoUsd = nuevoSaldoUsd; }

    public BigDecimal getNuevoSaldoCripto() { return nuevoSaldoCripto; }
    public void setNuevoSaldoCripto(BigDecimal nuevoSaldoCripto) { this.nuevoSaldoCripto = nuevoSaldoCripto; }
}