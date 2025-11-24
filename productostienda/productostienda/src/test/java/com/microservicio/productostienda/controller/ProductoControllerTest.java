package com.microservicio.productostienda.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservicio.productostienda.model.Categoria;
import com.microservicio.productostienda.model.Producto;
import com.microservicio.productostienda.service.ProductoService;
import com.microservicio.productostienda.util.JwtUtil;

@WebMvcTest(ProductoController.class)
// Deshabilita la seguridad para que los tests de permisos sean explícitos con JWT
@AutoConfigureMockMvc(addFilters = false) 
public class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc; 

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductoService productoService;

    @MockBean
    private JwtUtil jwtUtil; 

    private final String ADMIN_TOKEN = "Bearer valid_admin_token";
    private final String CLIENTE_TOKEN = "Bearer valid_cliente_token";
    private final String INVALID_TOKEN = "Bearer invalid_token";
    
    private Producto producto;
    private String productoPayloadJson;

    @BeforeEach
    void setUp() throws Exception {
        Categoria categoria = new Categoria(1L, "Calzado");
        
        // ⬇️ AJUSTE 1: Incluir los nuevos campos 'stock' (10) e 'imagenUri' (null o una cadena)
        producto = new Producto(10L, "Zapatillas Air", "Correr", 150.0, 
                                10, "zapatillas.jpg", categoria); // Constructor ajustado

        
        Map<String, Object> payload = new HashMap<>();
        payload.put("nombre", "Zapatillas Air");
        payload.put("descripcion", "Correr");
        payload.put("precio", 150.0);
        // ⬇️ AJUSTE 2: Incluir el nuevo campo 'stock' en el payload de la petición (Map)
        payload.put("stock", 10); 
        // Nota: 'imagenUri' no se necesita en el payload si es opcional o se maneja aparte.
        payload.put("categoriaId", 1L);
        productoPayloadJson = objectMapper.writeValueAsString(payload);
        
        
        when(jwtUtil.extractRole(ADMIN_TOKEN)).thenReturn("ADMINISTRADOR");
        when(jwtUtil.extractRole(CLIENTE_TOKEN)).thenReturn("CLIENTE");
        when(jwtUtil.extractRole(INVALID_TOKEN)).thenReturn(null);
    }

    
    // Test: Listar productos (Acceso público)
    @Test
    void testListarProductos_RetornaOk() throws Exception {
        when(productoService.listarProductos()).thenReturn(Arrays.asList(producto));

        mockMvc.perform(get("/api/productos")
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk());
        
        verify(productoService, times(1)).listarProductos();
    }
    
    // Test: Obtener por ID (Acceso público)
    @Test
    void testObtenerProductoPorId_Encontrado_RetornaOk() throws Exception {
        when(productoService.obtenerPorId(10L)).thenReturn(Optional.of(producto));

        mockMvc.perform(get("/api/productos/{id}", 10L))
                .andExpect(status().isOk());
    }

    // Test: Obtener por ID (No encontrado)
    @Test
    void testObtenerProductoPorId_NoEncontrado_RetornaNotFound() throws Exception {
        when(productoService.obtenerPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/productos/{id}", 99L))
                .andExpect(status().isNotFound());
    }
    
    
    // Test: Crear producto (Acceso ADMIN)
    @Test
    void testCrearProducto_ComoAdmin_RetornaCreated() throws Exception {
        when(productoService.guardarProducto(any(Producto.class))).thenReturn(producto);

        mockMvc.perform(post("/api/productos")
                .header("Authorization", ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(productoPayloadJson)) 
                
                .andExpect(status().isCreated()); 
        
        verify(productoService, times(1)).guardarProducto(any(Producto.class));
    }

    // Test: Crear producto (Acceso CLIENTE - Prohibido)
    @Test
    void testCrearProducto_ComoCliente_RetornaForbidden() throws Exception {
        mockMvc.perform(post("/api/productos")
                .header("Authorization", CLIENTE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(productoPayloadJson)) 
                
                .andExpect(status().isForbidden()); 
        
        verify(productoService, never()).guardarProducto(any(Producto.class));
    }
    
    // Test: Crear producto (Token inválido - No autorizado)
    @Test
    void testCrearProducto_TokenInvalido_RetornaUnauthorized() throws Exception {
        mockMvc.perform(post("/api/productos")
                .header("Authorization", INVALID_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(productoPayloadJson)) 
                
                .andExpect(status().isUnauthorized()); 
        
        verify(productoService, never()).guardarProducto(any(Producto.class));
    }

    
    // Test: Editar producto (Acceso ADMIN - Encontrado)
    @Test
    void testEditarProducto_ComoAdmin_RetornaOk() throws Exception {
        when(productoService.obtenerPorId(10L)).thenReturn(Optional.of(producto));
        when(productoService.guardarProducto(any(Producto.class))).thenReturn(producto);

        mockMvc.perform(put("/api/productos/{id}", 10L)
                .header("Authorization", ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(productoPayloadJson))
                
                .andExpect(status().isOk());
        
        verify(productoService, times(1)).guardarProducto(any(Producto.class));
    }
    
    // Test: Editar producto (Acceso ADMIN - No encontrado)
    @Test
    void testEditarProducto_NoEncontrado_RetornaNotFound() throws Exception {
        when(productoService.obtenerPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/productos/{id}", 99L)
                .header("Authorization", ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(productoPayloadJson))
                
                .andExpect(status().isNotFound());
        
        verify(productoService, never()).guardarProducto(any(Producto.class));
    }

    // Test: Eliminar producto (Acceso ADMIN)
    @Test
    void testEliminarProducto_ComoAdmin_RetornaNoContent() throws Exception {
        mockMvc.perform(delete("/api/productos/{id}", 10L)
                .header("Authorization", ADMIN_TOKEN))
                
                .andExpect(status().isNoContent()); 
        
        verify(productoService, times(1)).eliminarProducto(10L);
    }

    // Test: Eliminar producto (Acceso CLIENTE - Prohibido)
    @Test
    void testEliminarProducto_ComoCliente_RetornaForbidden() throws Exception {
        mockMvc.perform(delete("/api/productos/{id}", 10L)
                .header("Authorization", CLIENTE_TOKEN))
                
                .andExpect(status().isForbidden());
        
        verify(productoService, never()).eliminarProducto(anyLong());
    }
}