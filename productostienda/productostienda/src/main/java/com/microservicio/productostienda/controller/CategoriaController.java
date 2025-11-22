package com.microservicio.productostienda.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservicio.productostienda.model.Categoria;
import com.microservicio.productostienda.service.CategoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("api/categorias")
public class CategoriaController {
    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }
    @Operation(summary = "Listar todas las categorías", description = "Obtiene una lista de todas las categorías disponibles")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de categorías obtenida correctamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public List<Categoria> listar() {
        return categoriaService.listarCategorias();
    }
    @Operation(summary = "Crear una nueva categoría", description = "Permite crear una nueva categoría en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Categoría creada correctamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })

    @PostMapping
    public Categoria crear(@RequestBody Categoria categoria) {
        return categoriaService.guardarCategoria(categoria);
    }

}
