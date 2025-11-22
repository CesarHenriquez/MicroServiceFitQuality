package com.microservicio.registrousuario.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.microservicio.registrousuario.model.Rol;
import com.microservicio.registrousuario.repository.RolRepository;

@Configuration
public class LoadDataBase {
    @Bean
     CommandLineRunner initDatabase(RolRepository rolRepository) {
        return args -> {
            if (rolRepository.count() == 0) {
                rolRepository.save(new Rol(null, "CLIENTE"));
                rolRepository.save(new Rol(null, "DELIVERY"));
                rolRepository.save(new Rol(null, "ADMINISTRADOR"));
               
            }
        };
    }

}
