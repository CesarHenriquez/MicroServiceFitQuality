package com.microservicio.direcciones.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservicio.direcciones.model.Region;
import com.microservicio.direcciones.service.RegionService;

@RestController
@RequestMapping("/api/regiones")
public class RegionController {
     private final RegionService regionService;

    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    @GetMapping
    public List<Region> listar() {
        return regionService.listar();
    }

    @PostMapping
    public Region guardar(@RequestBody Region region) {
        return regionService.guardar(region);
    }

}
