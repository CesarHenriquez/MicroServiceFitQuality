package com.microservicio.productostienda.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.microservicio.productostienda.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

}
