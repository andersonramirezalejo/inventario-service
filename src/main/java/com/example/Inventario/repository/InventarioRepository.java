package com.example.Inventario.repository;

import com.example.Inventario.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    // JpaRepository CRUD básicos para Inventario usando productoId como PK.
}
