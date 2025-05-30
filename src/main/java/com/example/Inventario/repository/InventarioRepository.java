package com.example.Inventario.repository;

import com.example.Inventario.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad {@link Inventario}.
 * <p>
 * Proporciona operaciones CRUD y consultas sobre Inventario utilizando Spring Data JPA.
 * Los métodos básicos como guardar, buscar, actualizar y eliminar son heredados de {@link JpaRepository}.
 * </p>
 */
@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {}
