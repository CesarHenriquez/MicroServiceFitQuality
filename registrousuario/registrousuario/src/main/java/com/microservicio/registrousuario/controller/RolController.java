package com.microservicio.registrousuario.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.microservicio.registrousuario.model.Rol;
import com.microservicio.registrousuario.service.RolService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/roles")
public class RolController {
    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @Operation(summary = "Listar roles", description = "Obtiene una lista de todos los roles disponibles")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de roles obtenida exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado, se requiere rol de administrador")
    })
    @GetMapping
    public CollectionModel<EntityModel<Rol>> listar() {
        List<Rol> roles = rolService.listarRoles();

        List<EntityModel<Rol>> rolesModel = roles.stream()
            .map(rol -> EntityModel.of(rol,
                    linkTo(methodOn(RolController.class).obtener(rol.getId())).withSelfRel()))
            .collect(Collectors.toList());

        return CollectionModel.of(rolesModel,
                linkTo(methodOn(RolController.class).listar()).withSelfRel());
    }

    @Operation(summary = "Obtener rol por ID", description = "Obtiene un rol específico por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rol obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Rol no encontrado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado, se requiere rol de administrador")
    })
    @GetMapping("/{id}")
    public EntityModel<Rol> obtener(@PathVariable Long id) {
        Rol rol = rolService.obtenerPorId(id).orElse(null);
        return EntityModel.of(rol,
                linkTo(methodOn(RolController.class).obtener(id)).withSelfRel(),
                linkTo(methodOn(RolController.class).listar()).withRel("todos-los-roles"));
    }

    @Operation(summary = "Crear rol", description = "Crea un nuevo rol en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Rol creado exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado, se requiere rol de administrador")
    })
    @PostMapping
    public EntityModel<Rol> crear(@RequestBody Rol rol) {
        Rol creado = rolService.guardarRol(rol);
        return EntityModel.of(creado,
                linkTo(methodOn(RolController.class).obtener(creado.getId())).withSelfRel(),
                linkTo(methodOn(RolController.class).listar()).withRel("todos-los-roles"));
    }

}
