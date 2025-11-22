package com.microservicio.productostienda.controller;

import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RestController;


import com.microservicio.productostienda.model.Categoria;
import com.microservicio.productostienda.model.Producto;
import com.microservicio.productostienda.service.ProductoService;
import com.microservicio.productostienda.util.JwtUtil; 

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoService productoService;
    private final JwtUtil jwtUtil; 

    
    public ProductoController(ProductoService productoService, JwtUtil jwtUtil) {
        this.productoService = productoService;
        this.jwtUtil = jwtUtil;
    }

    
    
    @Operation(summary = "Listar todos los productos", description = "Obtiene una lista de todos los productos disponibles")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de productos obtenida correctamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public List<Producto> listar() {
        return productoService.listarProductos();
    }

    @Operation(summary = "Obtener producto por ID", description = "Obtiene un producto específico por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto encontrado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtener(@PathVariable Long id) {
        return productoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    
    
    
    private ResponseEntity<String> validarRolAdministrador(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT no proporcionado o formato inválido.");
        }
        
       
        String rol = jwtUtil.extractRole(authorizationHeader);
        
        if (rol == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT inválido o expirado.");
        }
        
      
        if (!"ADMINISTRADOR".equalsIgnoreCase(rol)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: solo usuarios ADMINISTRADOR pueden realizar esta acción.");
        }
        
        return null; 
    }

    

    @Operation(summary = "Crear un nuevo producto", description = "Permite crear un nuevo producto en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Producto creado correctamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado, solo usuarios ADMINISTRADOR pueden crear productos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<?> crear(
        @RequestBody Map<String, Object> payload, 
        // Parámetro para recibir el JWT
        @RequestHeader("Authorization") String authorizationHeader 
    ) {
        ResponseEntity<String> validationResult = validarRolAdministrador(authorizationHeader);
        if (validationResult != null) {
            return validationResult; // Devuelve error si el token o rol es inválido
        }

        try {
            
            
            Producto producto = new Producto();
            producto.setNombre((String) payload.get("nombre"));
            producto.setDescripcion((String) payload.get("descripcion"));
            producto.setPrecio(Double.valueOf(payload.get("precio").toString()));

            Categoria categoria = new Categoria();
            categoria.setId(Long.valueOf(payload.get("categoriaId").toString()));
            producto.setCategoria(categoria);

            Producto creado = productoService.guardarProducto(producto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear producto: " + e.getMessage());
        }
    }

    @Operation(summary = "Editar un producto existente", description = "Permite editar un producto existente en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto editado correctamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado, solo usuarios ADMINISTRADOR pueden editar productos"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> editar(
        @PathVariable Long id, 
        @RequestBody Map<String, Object> payload,
        // Parámetro para recibir el JWT
        @RequestHeader("Authorization") String authorizationHeader 
    ) {
        ResponseEntity<String> validationResult = validarRolAdministrador(authorizationHeader);
        if (validationResult != null) {
            return validationResult; // Devuelve error si el token o rol es inválido
        }

        try {
            

            return productoService.obtenerPorId(id).map(producto -> {
                producto.setNombre((String) payload.get("nombre"));
                producto.setDescripcion((String) payload.get("descripcion"));
                producto.setPrecio(Double.valueOf(payload.get("precio").toString()));

                Categoria categoria = new Categoria();
                categoria.setId(Long.valueOf(payload.get("categoriaId").toString()));
                producto.setCategoria(categoria);

                Producto actualizado = productoService.guardarProducto(producto);
                return ResponseEntity.ok(actualizado);
            }).orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al editar producto: " + e.getMessage());
        }
    }

    @Operation(summary = "Eliminar un producto", description = "Permite eliminar un producto del sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Producto eliminado correctamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado, solo usuarios ADMINISTRADOR pueden eliminar productos"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
        @PathVariable Long id,
        // Parámetro para recibir el JWT (reemplaza nickname y clave)
        @RequestHeader("Authorization") String authorizationHeader 
    ) {
        ResponseEntity<String> validationResult = validarRolAdministrador(authorizationHeader);
        if (validationResult != null) {
            return validationResult; // Devuelve error si el token o rol es inválido
        }
        
        try {
            

            productoService.eliminarProducto(id);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar producto: " + e.getMessage());
        }
    }
}