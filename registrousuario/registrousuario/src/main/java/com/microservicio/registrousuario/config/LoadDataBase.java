package com.microservicio.registrousuario.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.microservicio.registrousuario.model.Rol;
import com.microservicio.registrousuario.model.Usuario;
import com.microservicio.registrousuario.repository.RolRepository;
import com.microservicio.registrousuario.repository.UsuarioRepository;

@Configuration
public class LoadDataBase {
    @Bean
    CommandLineRunner initDatabase(RolRepository rolRepository, UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
         

            Rol clienteRol = rolRepository.findByNombre("CLIENTE")
                    .orElseGet(() -> rolRepository.save(new Rol(null, "CLIENTE")));

            Rol deliveryRol = rolRepository.findByNombre("DELIVERY")
                    .orElseGet(() -> rolRepository.save(new Rol(null, "DELIVERY")));

            Rol adminRol = rolRepository.findByNombre("ADMINISTRADOR")
                    .orElseGet(() -> rolRepository.save(new Rol(null, "ADMINISTRADOR")));

          
            if (usuarioRepository.findByNickname("admin1").isEmpty() && adminRol != null) {

                final String ADMIN_CLAVE_ENCRIPTADA = "$2a$10$xM5sJhMPgpAmd66RNB7jOO/nqcArrMGPJpMfvnx4OSOz1Pd0HsfYS";

                Usuario admin = new Usuario(
                        null, 
                        "admin1",
                        ADMIN_CLAVE_ENCRIPTADA,
                        "admin@bootstrap.com",
                        adminRol 
                );
                usuarioRepository.save(admin);
            }
        };
    }
}