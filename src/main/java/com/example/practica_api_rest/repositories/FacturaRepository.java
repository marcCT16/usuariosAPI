package com.example.practica_api_rest.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.practica_api_rest.models.entities.Factura;

public interface FacturaRepository extends JpaRepository<Factura, Long> {

	List<Factura> findAllByOrderByFechaDesc();

	List<Factura> findByUsuario_ClaveOrderByFechaDesc(String clave);

	boolean existsByUsuario_Clave(String clave);

}