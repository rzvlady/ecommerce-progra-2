package com.sv.grupo1.ecommerce.config;

import com.sv.grupo1.backoffice.service.ProductoImagenStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ProductoImagenStorageService imagenStorageService;

    public WebMvcConfig(ProductoImagenStorageService imagenStorageService) {
        this.imagenStorageService = imagenStorageService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path storageDir = imagenStorageService.getBaseStorageDir();
        String locationUri = storageDir.toUri().toString();
        if (!locationUri.endsWith("/")) {
            locationUri += "/";
        }

        registry.addResourceHandler("/uploads/productos/**")
                .addResourceLocations(locationUri)
                .setCachePeriod(0);
    }
}
