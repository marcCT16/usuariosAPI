package com.example.practica_api_rest.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.example.practica_api_rest.dtos.ActualizarStockDTO;
import com.example.practica_api_rest.dtos.ProductoDTO;
import com.example.practica_api_rest.models.entities.Producto;
import com.example.practica_api_rest.repositories.DetalleFacturaRepository;
import com.example.practica_api_rest.repositories.ProductoRepository;

@Service
public class ProductoService {

	private final ProductoRepository repository;
	private final DetalleFacturaRepository detalleRepository;

	public ProductoService(ProductoRepository repository, DetalleFacturaRepository detalleRepository) {
		this.repository = repository;
		this.detalleRepository = detalleRepository;
	}

	public List<ProductoDTO> listar() {
		return repository.findAll().stream().map(this::aDTO).toList();
	}

	public ProductoDTO buscarPorId(Long id) {
		return repository.findById(id).map(this::aDTO).orElseThrow(() -> new NoEncontradoException(id));
	}

	public ProductoDTO crear(ProductoDTO dto) {
		validar(dto);
		return aDTO(repository.save(aEntidad(dto)));
	}

	public ProductoDTO actualizar(Long id, ProductoDTO dto) {
		if (!repository.existsById(id)) {
			throw new NoEncontradoException(id);
		}
		validar(dto);
		Producto producto = aEntidad(dto);
		producto.setId(id);
		return aDTO(repository.save(producto));
	}

	public ProductoDTO aumentarStock(Long id, ActualizarStockDTO dto) {
		if (dto == null || dto.getUnidades() == null || dto.getUnidades() <= 0) {
			throw new InvalidaException("unidades debe ser mayor que cero");
		}
		Producto producto = repository.findById(id).orElseThrow(() -> new NoEncontradoException(id));
		if (producto.getStock() > Integer.MAX_VALUE - dto.getUnidades()) {
			throw new InvalidaException("el stock resultante supera el limite permitido");
		}
		producto.setStock(producto.getStock() + dto.getUnidades());
		return aDTO(repository.save(producto));
	}

	public void eliminar(Long id) {
		if (!repository.existsById(id)) {
			throw new NoEncontradoException(id);
		}
		if (detalleRepository.existsByProducto_Id(id)) {
			throw new EnUsoException(id);
		}
		repository.deleteById(id);
	}

	private void validar(ProductoDTO dto) {
		if (dto == null || dto.getNombre() == null || dto.getNombre().isBlank()) {
			throw new InvalidaException("nombre es obligatorio");
		}
		if (dto.getNombre().length() > 120) {
			throw new InvalidaException("nombre supera 120 caracteres");
		}
		if (dto.getDescripcion() != null && dto.getDescripcion().length() > 500) {
			throw new InvalidaException("descripcion supera 500 caracteres");
		}
		if (dto.getPrecio() == null || dto.getPrecio().signum() <= 0) {
			throw new InvalidaException("precio debe ser mayor que cero");
		}
		if (dto.getPrecio().scale() > 2 || dto.getPrecio().precision() > 10) {
			throw new InvalidaException("precio debe tener maximo 10 digitos y 2 decimales");
		}
		if (dto.getStock() == null || dto.getStock() < 0) {
			throw new InvalidaException("stock debe ser cero o mayor");
		}
	}

	private Producto aEntidad(ProductoDTO dto) {
		Producto producto = new Producto();
		producto.setNombre(dto.getNombre().trim());
		producto.setDescripcion(dto.getDescripcion());
		producto.setPrecio(dto.getPrecio());
		producto.setStock(dto.getStock());
		return producto;
	}

	private ProductoDTO aDTO(Producto producto) {
		ProductoDTO dto = new ProductoDTO();
		dto.setId(producto.getId());
		dto.setNombre(producto.getNombre());
		dto.setDescripcion(producto.getDescripcion());
		dto.setPrecio(producto.getPrecio());
		dto.setStock(producto.getStock());
		return dto;
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	public static class NoEncontradoException extends RuntimeException {
		public NoEncontradoException(Long id) {
			super("Producto con id " + id + " no existe");
		}
	}

	@ResponseStatus(HttpStatus.CONFLICT)
	public static class EnUsoException extends RuntimeException {
		public EnUsoException(Long id) {
			super("No se puede eliminar el producto con id " + id + " porque aparece en facturas");
		}
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public static class InvalidaException extends RuntimeException {
		public InvalidaException(String mensaje) {
			super(mensaje);
		}
	}

}