package com.microservicio.productostienda.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
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
    private Producto productoEntrante;
    private Producto productoGuardado;

    @BeforeEach
    void setUp() {
        categoria = new Categoria(1L, "Accesorios");
        
        // 1. Producto entrante (el que se recibe del controlador, solo tiene el ID de la categoría)
        Producto productoRecibido = new Producto();
        productoRecibido.setNombre("Straps");
        productoRecibido.setDescripcion("Correas de agarre");
        productoRecibido.setPrecio(10.0);
        productoRecibido.setStock(50); // Nuevo campo
        productoRecibido.setImagenUri("straps.jpg"); // Nuevo campo
        productoRecibido.setCategoria(new Categoria(1L, null)); // Solo contiene el ID
        this.productoEntrante = productoRecibido;

        // 2. Producto resultante (el que se guarda en el repositorio, tiene el objeto Categoria completo)
        productoGuardado = new Producto(1L, "Straps", "Correas de agarre", 10.0, 
                                        50, "straps.jpg", categoria);
    }

    // --- Tests para guardar/crear/actualizar producto ---

    @Test
    void testGuardarProducto_ExisteCategoria_RetornaProductoConCategoriaCompleta() {
        // Simula que la categoría es encontrada por su ID
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        // Simula la acción de guardar en la base de datos
        when(productoRepository.save(any(Producto.class))).thenReturn(productoGuardado);

        // Ejecutar el método
        Producto resultado = productoService.guardarProducto(productoEntrante);

        // Verificar
        assertNotNull(resultado);
        assertEquals(productoGuardado.getNombre(), resultado.getNombre());
        // CRÍTICO: Verificar que el objeto Categoria completo fue inyectado
        assertEquals(categoria.getNombre(), resultado.getCategoria().getNombre());
        // CRÍTICO: Verificar que los nuevos campos se mantengan
        assertEquals(productoGuardado.getStock(), resultado.getStock());
        assertEquals(productoGuardado.getImagenUri(), resultado.getImagenUri());
        
        // Verificar interacciones
        verify(categoriaRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void testGuardarProducto_CategoriaNoExiste_LanzaRuntimeException() {
        // Simula que la categoría NO es encontrada
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        // Verificar que se lance la excepción esperada
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productoService.guardarProducto(productoEntrante);
        });

        // Verificar mensaje de error
        assertTrue(exception.getMessage().contains("Categoría no encontrada con ID: 1"));
        
        // Verificar interacciones
        verify(categoriaRepository, times(1)).findById(1L);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    // --- Tests para listar productos ---

    @Test
    void testListarProductos_RetornaLista() {
        List<Producto> listaEsperada = Arrays.asList(productoGuardado);
        when(productoRepository.findAll()).thenReturn(listaEsperada);

        List<Producto> resultado = productoService.listarProductos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(productoGuardado.getNombre(), resultado.get(0).getNombre());
        
        verify(productoRepository, times(1)).findAll();
    }

    // --- Tests para obtener por ID ---

    @Test
    void testObtenerPorId_Existe_RetornaOptionalProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoGuardado));

        Optional<Producto> resultado = productoService.obtenerPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(productoGuardado.getNombre(), resultado.get().getNombre());
        
        verify(productoRepository, times(1)).findById(1L);
    }

    @Test
    void testObtenerPorId_NoExiste_RetornaOptionalVacio() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Producto> resultado = productoService.obtenerPorId(99L);

        assertFalse(resultado.isPresent());
        
        verify(productoRepository, times(1)).findById(99L);
    }

    // --- Tests para eliminar producto ---

    @Test
    void testEliminarProducto_EliminaCorrectamente() {
        // No hay retorno de valor, solo verificamos la ejecución
        doNothing().when(productoRepository).deleteById(1L);

        productoService.eliminarProducto(1L);

        // Verificar que se llamó al método deleteById una vez con el ID correcto
        verify(productoRepository, times(1)).deleteById(1L);
    }
}