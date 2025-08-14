package com.trading.cripto.model;


import jakarta.persistence.*; // Importaciones de Jakarta Persistence
import java.sql.Date;

@Entity // 1. Le dice a JPA que esta clase es una tabla en la BD
@Table(name = "users") // 2. Especifica el nombre exacto de la tabla
public class User {

    @Id // 3. Marca este campo como la clave primaria (Primary Key)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 4. Le dice a la BD que genere el ID automáticamente
    private int id;

    @Column(name = "correo_electronico", nullable = false, unique = true) // 5. Mapea al nombre de la columna
    private String email;

    @Column(name = "contraseña_hash", nullable = false)
    private String password_hash;

    @Column(name = "nombre_usuario", nullable = false, unique = true)
    private String nombre_usuario;

    @Column(name = "nombre_completo", nullable = false)
    private String nombre_completo;

    @Column(name = "fecha_registro")
    private Date fecha_registro;

    @Column(name = "fecha_nac")
    private Date fecha_nacimiento;

    // Getters
    public int getId() {
        return id;
    }
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password_hash;
    }

    public String getNombreUsuario() {
        return nombre_usuario;
    }

    public String getNombreCompleto() {
        return nombre_completo;
    }

    public Date getFechaRegistro() {
        return fecha_registro;
    }

    public Date getFechaNacimiento() {
        return fecha_nacimiento;
    }

    // Setters

    public void setId(int id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password_hash = password;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombre_usuario = nombreUsuario;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombre_completo = nombreCompleto;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fecha_registro = fechaRegistro;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fecha_nacimiento = fechaNacimiento;
    }
}
