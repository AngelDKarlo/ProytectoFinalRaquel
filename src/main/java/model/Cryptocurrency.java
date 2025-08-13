package model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cripto")
public class Cryptocurrency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "simbolo", nullable = false, unique = true)
    private String simbolo;

    @Column(name = "precio", precision = 20, scale = 8)
    private BigDecimal precio;

    @Column(name = "descripcion")
    private String descripcion;

    public Integer getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public String getSimbolo() {
        return simbolo;
    }
    public BigDecimal getPrecio() {
        return precio;
    }
    public String getDescripcion() {
        return descripcion;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }
    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
