package com.example.ocr;

public class Nutriente {
    private String nombre;
    private String valor;
    private String unidad;

    public Nutriente() {}
    public Nutriente(String nombre, String valor, String unidad) {
        this.nombre = nombre;
        this.valor = valor;
        this.unidad = unidad;
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
}