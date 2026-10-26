package com.sv.grupo1.backoffice.task;

import com.sv.grupo1.ecommerce.entities.core.Producto;

import java.util.List;

public interface StockMonitorTask {

    List<Producto> getProductosBajoStockAlertados();

    void escanearInventario();

}
