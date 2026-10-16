package com.example.practica_api_rest.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.example.practica_api_rest.dtos.CrearFacturaDTO;
import com.example.practica_api_rest.dtos.DetalleFacturaDTO;
import com.example.practica_api_rest.dtos.FacturaDTO;
import com.example.practica_api_rest.dtos.ItemFacturaDTO;
import com.example.practica_api_rest.models.entities.DetalleFactura;
import com.example.practica_api_rest.models.entities.Factura;
import com.example.practica_api_rest.models.entities.Producto;
import com.example.practica_api_rest.models.entities.Usuario;
import com.example.practica_api_rest.repositories.FacturaRepository;
import com.example.practica_api_rest.repositories.ProductoRepository;
import com.example.practica_api_rest.repositories.UsuarioRepository;

@Service
public class FacturaService {

	private final FacturaRepository repository;
	private final UsuarioRepository usuarioRepository;
	private final ProductoRepository productoRepository;

	public FacturaService(FacturaRepository repository, UsuarioRepository usuarioRepository,
			ProductoRepository productoRepository) {
		this.repository = repository;
		this.usuarioRepository = usuarioRepository;
		this.productoRepository = productoRepository;
	}

	@Transactional(readOnly = true)
	public List<FacturaDTO> listar(String usuarioClave) {
		List<Factura> facturas = usuarioClave == null || usuarioClave.isBlank()
				? repository.findAllByOrderByFechaDesc()
				: repository.findByUsuario_ClaveOrderByFechaDesc(usuarioClave);
		return facturas.stream().map(this::aDTO).toList();
	}

	@Transactional(readOnly = true)
	public FacturaDTO buscarPorId(Long id) {
		return repository.findById(id).map(this::aDTO).orElseThrow(() -> new NoEncontradaException(id));
	}

	@Transactional
	public FacturaDTO crear(CrearFacturaDTO dto) {
		if (dto == null || dto.getUsuarioClave() == null || dto.getUsuarioClave().isBlank()) {
			throw new InvalidaException("usuarioClave es obligatorio");
		}
		if (dto.getProductos() == null || dto.getProductos().isEmpty()) {
			throw new InvalidaException("la factura debe incluir al menos un producto");
		}

		Usuario usuario = usuarioRepository.findById(dto.getUsuarioClave())
				.orElseThrow(() -> new UsuarioService.NoEncontradaException(dto.getUsuarioClave()));
		Factura factura = new Factura();
		factura.setUsuario(usuario);
		factura.setFecha(LocalDateTime.now());

		Set<Long> productosAgregados = new HashSet<>();
		BigDecimal total = BigDecimal.ZERO;
		for (ItemFacturaDTO item : dto.getProductos()) {
			if (item == null || item.getProductoId() == null) {
				throw new InvalidaException("productoId es obligatorio para cada producto");
			}
			if (item.getCantidad() == null || item.getCantidad() <= 0) {
				throw new InvalidaException("la cantidad debe ser mayor que cero");
			}
			if (!productosAgregados.add(item.getProductoId())) {
				throw new InvalidaException("no repita productos; indique la cantidad total en una sola linea");
			}

			Producto producto = productoRepository.findById(item.getProductoId())
					.orElseThrow(() -> new ProductoService.NoEncontradoException(item.getProductoId()));
			if (producto.getStock() == null || producto.getStock() < item.getCantidad()) {
				throw new SinStockException(producto.getId());
			}

			producto.setStock(producto.getStock() - item.getCantidad());
			DetalleFactura detalle = new DetalleFactura();
			detalle.setFactura(factura);
			detalle.setProducto(producto);
			detalle.setCantidad(item.getCantidad());
			detalle.setPrecioUnitario(producto.getPrecio());
			factura.getDetalles().add(detalle);
			total = total.add(producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad())));
		}

		factura.setTotal(total);
		return aDTO(repository.save(factura));
	}

	private FacturaDTO aDTO(Factura factura) {
		FacturaDTO dto = new FacturaDTO();
		dto.setId(factura.getId());
		dto.setUsuarioClave(factura.getUsuario().getClave());
		dto.setFecha(factura.getFecha());
		dto.setTotal(factura.getTotal());
		dto.setProductos(factura.getDetalles().stream().map(this::aDetalleDTO).toList());
		return dto;
	}

	private DetalleFacturaDTO aDetalleDTO(DetalleFactura detalle) {
		DetalleFacturaDTO dto = new DetalleFacturaDTO();
		dto.setProductoId(detalle.getProducto().getId());
		dto.setNombreProducto(detalle.getProducto().getNombre());
		dto.setCantidad(detalle.getCantidad());
		dto.setPrecioUnitario(detalle.getPrecioUnitario());
		dto.setSubtotal(detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad())));
		return dto;
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	public static class NoEncontradaException extends RuntimeException {
		public NoEncontradaException(Long id) {
			super("Factura con id " + id + " no existe");
		}
	}

	@ResponseStatus(HttpStatus.CONFLICT)
	public static class SinStockException extends RuntimeException {
		public SinStockException(Long productoId) {
			super("No hay stock suficiente para el producto con id " + productoId);
		}
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public static class InvalidaException extends RuntimeException {
		public InvalidaException(String mensaje) {
			super(mensaje);
		}
	}

}