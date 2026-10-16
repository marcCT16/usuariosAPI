package com.example.practica_api_rest.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.practica_api_rest.dtos.ActualizarStockDTO;
import com.example.practica_api_rest.dtos.ProductoDTO;
import com.example.practica_api_rest.services.ProductoService;

@RestController
@RequestMapping("/productos")
public class ProductoController {

	private final ProductoService service;

	public ProductoController(ProductoService service) {
		this.service = service;
	}

	@GetMapping
	public List<ProductoDTO> listar() {
		return service.listar();
	}

	@GetMapping("/{id}")
	public ProductoDTO buscar(@PathVariable Long id) {
		return service.buscarPorId(id);
	}

	@PostMapping
	public ResponseEntity<ProductoDTO> crear(@RequestBody ProductoDTO dto) {
		ProductoDTO creado = service.crear(dto);
		return ResponseEntity.created(URI.create("/productos/" + creado.getId())).body(creado);
	}

	@PutMapping("/{id}")
	public ProductoDTO actualizar(@PathVariable Long id, @RequestBody ProductoDTO dto) {
		return service.actualizar(id, dto);
	}

	@PutMapping("/{id}/stock")
	public ProductoDTO aumentarStock(@PathVariable Long id, @RequestBody ActualizarStockDTO dto) {
		return service.aumentarStock(id, dto);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		service.eliminar(id);
		return ResponseEntity.noContent().build();
	}

}