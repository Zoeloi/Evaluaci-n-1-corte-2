package com.example.demo.model;

import java.math.BigDecimal;

public class Producto {
    private int idProducto;
    private String codigo;
    private String nombre;
    private String categoria;
    private BigDecimal precio; // Requerimiento específico del laboratorio
    private int stock;

    public Producto(int idProducto, String codigo, String nombre, String categoria, BigDecimal precio, int stock) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
    }


    public int getIdProducto() { return idProducto; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public BigDecimal getPrecio() { return precio; }
    public int getStock() { return stock; }


    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public void setStock(int stock) { this.stock = stock; }
}