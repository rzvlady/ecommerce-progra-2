package com.sv.grupo1.ecommerce.entities.catalogo;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "categorias", schema = "catalogo")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria_padre")
    private Categoria categoriaPadre;

    @Column(name = "nombre_categoria", nullable = false)
    private String nombreCategoria;

    @Column(name = "descripcion_categoria", length = 255)
    private String descripcion;

    @Column(name = "estado", length = 1, nullable = false)
    private Character estado = 'A';

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    public Categoria() {}

    // Getters y Setters
    public Integer getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }
    public Categoria getCategoriaPadre() { return categoriaPadre; }
    public void setCategoriaPadre(Categoria categoriaPadre) { this.categoriaPadre = categoriaPadre; }
    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String nombreCategoria) { this.nombreCategoria = nombreCategoria; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Character getEstado() { return estado; }
    public void setEstado(Character estado) { this.estado = estado; }
    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public int getNivel() {
        int nivel = 1;
        Categoria actual = this.categoriaPadre;
        while (actual != null && nivel < 3) {
            nivel++;
            actual = actual.getCategoriaPadre();
        }
        return nivel;
    }

    public String getRutaJerarquica() {
        java.util.LinkedList<String> partes = new java.util.LinkedList<>();
        Categoria actual = this;
        int guard = 0;
        while (actual != null && guard < 4) {
            partes.addFirst(actual.getNombreCategoria());
            actual = actual.getCategoriaPadre();
            guard++;
        }
        return String.join(" › ", partes);
    }

    public String getRutaPadres() {
        java.util.LinkedList<String> partes = new java.util.LinkedList<>();
        Categoria actual = this.categoriaPadre;
        int guard = 0;
        while (actual != null && guard < 3) {
            partes.addFirst(actual.getNombreCategoria());
            actual = actual.getCategoriaPadre();
            guard++;
        }
        return String.join(" › ", partes);
    }

    public String getIdsJerarquia() {
        StringBuilder sb = new StringBuilder(",");
        Categoria actual = this;
        int guard = 0;
        while (actual != null && guard < 4) {
            if (actual.getIdCategoria() != null) {
                sb.append(actual.getIdCategoria()).append(",");
            }
            actual = actual.getCategoriaPadre();
            guard++;
        }
        return sb.toString();
    }

    public boolean perteneceAJerarquia(Integer idBuscado) {
        if (idBuscado == null) return true;
        Categoria actual = this;
        int guard = 0;
        while (actual != null && guard < 4) {
            if (idBuscado.equals(actual.getIdCategoria())) {
                return true;
            }
            actual = actual.getCategoriaPadre();
            guard++;
        }
        return false;
    }
}
