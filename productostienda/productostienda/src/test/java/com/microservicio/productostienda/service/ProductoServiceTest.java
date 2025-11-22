package com.microservicio.productostienda.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.microservicio.productostienda.model.Categoria;
import com.microservicio.productostienda.model.Producto;
import com.microservicio.productostienda.repository.CategoriaRepository;
import com.microservicio.productostienda.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;

    private Categoria categoria;
    private Producto producto;

    @BeforeEach
    void setUp() {
        categoria = new Categoria(1L, "Calzado");
        producto = new Producto(10L, "Zapatillas Air", "Correr", 150.0, categoria);
    }

    // --- TEST 1: guardarProducto - Éxito ---
    @Test
    void testGuardarProducto_CategoriaEncontrada_GuardaProducto() {
        // Simular que la categoría existe
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        // Simular el guardado en el repositorio de producto
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        Producto productoAGuardar = new Producto(null, "Zapatillas Air", "Correr", 150.0, new Categoria(1L, null));

        Producto resultado = productoService.guardarProducto(productoAGuardar);

        assertNotNull(resultado);
        assertEquals(categoria, resultado.getCategoria()); // Verifica que la categoría fue inyectada
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    // --- TEST 2: guardarProducto - Categoría No Encontrada ---
    @Test
    void testGuardarProducto_CategoriaNoEncontrada_LanzaExcepcion() {
        Long categoriaInvalidaId = 99L;
        // Simular que la categoría NO existe
        when(categoriaRepository.findById(categoriaInvalidaId)).thenReturn(Optional.empty());

        Producto productoAGuardar = new Producto(null, "Zapatillas", "Test", 100.0, new Categoria(categoriaInvalidaId, null));

        // Verificar que se lanza la RuntimeException
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            productoService.guardarProducto(productoAGuardar);
        });

        assertTrue(excepcion.getMessage().contains("Categoría no encontrada con ID: 99"));
        verify(productoRepository, never()).save(any(Producto.class));
    }
    
    // --- TEST 3: obtenerPorId y eliminarProducto ---
    
    @Test
    void testObtenerPorId() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        Optional<Producto> resultado = productoService.obtenerPorId(10L);
        assertTrue(resultado.isPresent());
    }

    @Test
    void testEliminarProducto() {
        productoService.eliminarProducto(10L);
        verify(productoRepository, times(1)).deleteById(10L);
    }
}
