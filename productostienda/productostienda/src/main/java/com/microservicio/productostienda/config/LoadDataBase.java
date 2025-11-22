package com.microservicio.productostienda.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.microservicio.productostienda.model.Categoria;
import com.microservicio.productostienda.repository.CategoriaRepository;
@Configuration
public class LoadDataBase {
     @Bean
    CommandLineRunner initDatabase(CategoriaRepository categoriaRepository) {
        return args -> {
            if (categoriaRepository.count() == 0) {
                categoriaRepository.save(new Categoria(null, "Accesorios"));
                categoriaRepository.save(new Categoria(null, "Pesas"));
              
            }
        };
    }

}
