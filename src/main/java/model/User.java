package model;


import java.security.PrivateKey;
import java.sql.Date;

public class User {
    private int id;
    private String email;
    private String password_hash;
    private String nombre_usuario;
    private String nombre_completo;
    private Date fecha_registro;
    private Date fecha_nacimiento;

    public int getId() {
        return id;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword_hash() {
        return password_hash;
    }
    public String getNombre_usuario() {
        return nombre_usuario;
    }
    public String getNombre_completo() {
        return nombre_completo;
    }
    public Date getFecha_registro() {
        return fecha_registro;
    }
    public Date getFecha_nacimiento() {
        return fecha_nacimiento;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }
    public void setNombre_usuario(String nombre_usuario) {
        this.nombre_usuario = nombre_usuario;
    }
    public void setNombre_completo(String nombre_completo) {
        this.nombre_completo = nombre_completo;
    }
    public void setFecha_registro(Date fecha_registro) {
        this.fecha_registro = fecha_registro;
    }
    public void setFecha_nacimiento(Date fecha_nacimiento) {
        this.fecha_nacimiento = fecha_nacimiento;
    }
}
