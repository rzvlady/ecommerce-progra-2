# Almacenamiento Segmentado de Fotografías de Productos

Esta carpeta almacena las fotografías de los productos del catálogo organizadas automáticamente según la **taxonomía jerárquica de categorías (hasta 3 niveles)** y el **código SKU** único de cada producto:

```text
uploads/productos/
├── <departamento-nivel-1>/
│   ├── <categoria-nivel-2>/
│   │   ├── <subcategoria-nivel-3>/
│   │   │   └── <CODIGO_SKU>/
│   │   │       ├── img-1.<ext>   # Bloque 1: Fotografía Principal
│   │   │       ├── img-2.<ext>   # Bloque 2: Fotografía Secundaria
│   │   │       ├── img-3.<ext>   # Bloque 3: Fotografía Adicional
│   │   │       └── img-4.<ext>   # Bloque 4: Fotografía Adicional
```

### Ejemplos de rutas generadas automáticamente:
- `uploads/productos/electronica/computacion/laptops/ELC-COM-LAP-HP-0001/img-1.webp`
- `uploads/productos/ropa/hombre/camisas/ROP-HOM-CAM-NIK-0047/img-1.jpg`
- `uploads/productos/hogar/electrodomesticos/linea-blanca/HOG-ELD-LIN-LG-0030/img-1.png`
- `uploads/productos/accesorios/ACC-XIA-0039/img-1.jpg`
