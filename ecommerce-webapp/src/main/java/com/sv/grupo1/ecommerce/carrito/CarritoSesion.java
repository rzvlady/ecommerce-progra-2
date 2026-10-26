package com.sv.grupo1.ecommerce.carrito;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** Carrito de un visitante: vive en su HttpSession (un bean por sesión). */
@Component
@SessionScope
public class CarritoSesion implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<Integer, CarritoItem> items = new LinkedHashMap<>();

    public Collection<CarritoItem> getItems() { return items.values(); }
    public CarritoItem get(Integer idProducto) { return items.get(idProducto); }
    public void poner(CarritoItem item) { items.put(item.getIdProducto(), item); }
    public void quitar(Integer idProducto) { items.remove(idProducto); }
    public void vaciar() { items.clear(); }
    public boolean estaVacio() { return items.isEmpty(); }
}
