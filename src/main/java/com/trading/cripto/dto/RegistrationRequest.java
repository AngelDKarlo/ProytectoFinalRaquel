package com.trading.cripto.dto;

import java.sql.Date;

public class RegistrationRequest {
    private String email;
    private String password;
    private String nombreUsuario;
    private String nombreCompleto;
    private java.sql.Date fechaRegistro;
    private java.sql.Date fechaNacimiento;

    // Getters
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
}
