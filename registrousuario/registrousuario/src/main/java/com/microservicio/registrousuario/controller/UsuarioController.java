package com.microservicio.registrousuario.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Arrays;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.microservicio.registrousuario.model.Usuario;
import com.microservicio.registrousuario.service.UsuarioService;
import com.microservicio.registrousuario.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    public UsuarioController(UsuarioService usuarioService, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }

   

    private ResponseEntity<String> validarRol(String authorizationHeader, String... requiredRoles) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token JWT no proporcionado o formato inválido.");
        }

        String rol = jwtUtil.extractRole(authorizationHeader);

        if (rol == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT inválido o expirado.");
        }

        boolean roleAllowed = Arrays.stream(requiredRoles)
                .anyMatch(r -> r.equalsIgnoreCase(rol));

        if (!roleAllowed) {
            String rolesStr = Arrays.toString(requiredRoles).replaceAll("[\\[\\]]", "");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: solo roles " + rolesStr + " pueden realizar esta acción.");
        }

        return null; // Éxito
    }

    @GetMapping("/interno/nickname/{nickname}")
    @ResponseStatus(HttpStatus.OK)
    public Usuario buscarPorNicknameInterno(@PathVariable String nickname) {
       
        return usuarioService.buscarPorNickname(nickname);
    }
  
    @Operation(summary = "Actualizar solo la clave", description = "Actualiza la clave de un usuario por su email")
    @PutMapping("/clave/{email}")
    public ResponseEntity<?> actualizarClave(@PathVariable String email, @RequestBody Map<String, String> payload) {
        String nuevaClave = payload.get("nuevaClave");

        if (nuevaClave == null || nuevaClave.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Falta el campo 'nuevaClave'.");
        }

        try {
            Usuario actualizado = usuarioService.actualizarClavePorEmail(email, nuevaClave);
            if (actualizado == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado para actualizar clave.");
            }
            return ResponseEntity.ok("Clave actualizada con éxito.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar la clave: " + e.getMessage());
        }
    }

   
    @GetMapping("/interno/email/{email}")
    public ResponseEntity<Usuario> buscarPorEmailInterno(@PathVariable String email) {
        Usuario usuario = usuarioService.buscarPorCorreo(email);

        if (usuario == null) {
           
            return ResponseEntity.notFound().build();
        }

       
        return ResponseEntity.ok(usuario);
    }
   
    @Operation(summary = "Registrar un nuevo usuario", description = "Crea un nuevo usuario en el sistema")
    @PostMapping
    public ResponseEntity<?> registrar(
            @RequestBody Usuario usuario
    
    ) {
      

        try {
           
           
            Usuario creado = usuarioService.crearUsuario(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (Exception e) {
          
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al crear usuario: " + e.getMessage());
        }
    }
    

    @Operation(summary = "Listar todos los usuarios", description = "Obtiene una lista de todos los usuarios registrados")
    @GetMapping
    public ResponseEntity<?> listar(@RequestHeader("Authorization") String authorizationHeader) {
       
        ResponseEntity<String> validationResult = validarRol(authorizationHeader, "ADMINISTRADOR");
        if (validationResult != null) {
            return validationResult;
        }

        List<Usuario> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    

    @Operation(summary = "Obtener usuario por ID", description = "Obtiene un usuario específico por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader) {
      
        ResponseEntity<String> validationResult = validarRol(authorizationHeader, "ADMINISTRADOR", "DELIVERY");
        if (validationResult != null) {
            return validationResult;
        }

        Optional<Usuario> optional = usuarioService.obtenerPorId(id);
        Usuario usuario = optional.orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }

        return ResponseEntity.ok(usuario);
    }

    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody Usuario usuario,
            @RequestHeader("Authorization") String authorizationHeader) {
        
        ResponseEntity<String> validationResult = validarRol(authorizationHeader, "ADMINISTRADOR");
        if (validationResult != null) {
            return validationResult;
        }

        Usuario actualizado = usuarioService.actualizarUsuario(id, usuario);
        if (actualizado == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado para actualizar.");
        }
        return ResponseEntity.ok(actualizado);
    }

    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario del sistema por su ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader) {
        
        ResponseEntity<String> validationResult = validarRol(authorizationHeader, "ADMINISTRADOR");
        if (validationResult != null) {
            return validationResult;
        }

        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}