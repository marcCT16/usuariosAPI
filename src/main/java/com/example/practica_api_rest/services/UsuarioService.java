package com.example.practica_api_rest.services;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.example.practica_api_rest.dtos.UsuarioDTO;
import com.example.practica_api_rest.models.entities.Usuario;
import com.example.practica_api_rest.repositories.FacturaRepository;
import com.example.practica_api_rest.repositories.UsuarioRepository;

@Service
public class UsuarioService {

	private static final int LIMITE_LONGITUD = 11;

	private final UsuarioRepository repository;
	private final FacturaRepository facturaRepository;

	public UsuarioService(UsuarioRepository repository, FacturaRepository facturaRepository) {
		this.repository = repository;
		this.facturaRepository = facturaRepository;
	}

	public List<UsuarioDTO> listar() {
		return repository.findAll().stream().map(this::aDTO).toList();
	}

	public UsuarioDTO buscarPorClave(String clave) {
		return repository.findById(clave).map(this::aDTO)
				.orElseThrow(() -> new NoEncontradaException(clave));
	}

	public UsuarioDTO crear(UsuarioDTO dto) {
		validar(dto);
		if (repository.existsById(dto.getClave())) {
			throw new DuplicadaException(dto.getClave());
		}
		Usuario guardado = repository.save(aEntidad(dto));
		return aDTO(guardado);
	}

	public UsuarioDTO actualizar(String clave, UsuarioDTO dto) {
		if (!repository.existsById(clave)) {
			throw new NoEncontradaException(clave);
		}
		validar(dto);
		if (!dto.getClave().equals(clave) && repository.existsById(dto.getClave())) {
			throw new DuplicadaException(dto.getClave());
		}
		Usuario entidad = aEntidad(dto);
		entidad.setClave(clave);
		return aDTO(repository.save(entidad));
	}

	public void eliminar(String clave) {
		if (!repository.existsById(clave)) {
			throw new NoEncontradaException(clave);
		}
		if (facturaRepository.existsByUsuario_Clave(clave)) {
			throw new TieneFacturasException(clave);
		}
		repository.deleteById(clave);
	}

	private void validar(UsuarioDTO dto) {
		if (dto.getClave() == null || dto.getClave().isBlank()) {
			throw new InvalidaException("clave es obligatoria");
		}
		if (dto.getNombre() == null || dto.getNombre().isBlank()) {
			throw new InvalidaException("nombre es obligatorio");
		}
		if (dto.getCodigo() == null || dto.getCodigo().isBlank()) {
			throw new InvalidaException("codigo es obligatorio");
		}
		if (dto.getClave().length() > LIMITE_LONGITUD) {
			throw new InvalidaException("clave supera " + LIMITE_LONGITUD + " caracteres");
		}
		if (dto.getNombre().length() > LIMITE_LONGITUD) {
			throw new InvalidaException("nombre supera " + LIMITE_LONGITUD + " caracteres");
		}
		if (dto.getCodigo().length() > LIMITE_LONGITUD) {
			throw new InvalidaException("codigo supera " + LIMITE_LONGITUD + " caracteres");
		}
	}

	private Usuario aEntidad(UsuarioDTO dto) {
		Usuario usuario = new Usuario();
		usuario.setClave(dto.getClave());
		usuario.setNombre(dto.getNombre());
		usuario.setCodigo(dto.getCodigo());
		return usuario;
	}

	private UsuarioDTO aDTO(Usuario usuario) {
		UsuarioDTO dto = new UsuarioDTO();
		dto.setClave(usuario.getClave());
		dto.setNombre(usuario.getNombre());
		dto.setCodigo(usuario.getCodigo());
		return dto;
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	public static class NoEncontradaException extends RuntimeException {
		public NoEncontradaException(String clave) {
			super("Usuario con clave '" + clave + "' no existe");
		}
	}

	@ResponseStatus(HttpStatus.CONFLICT)
	public static class DuplicadaException extends RuntimeException {
		public DuplicadaException(String clave) {
			super("Ya existe un usuario con clave '" + clave + "'");
		}
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public static class InvalidaException extends RuntimeException {
		public InvalidaException(String mensaje) {
			super(mensaje);
		}
	}

	@ResponseStatus(HttpStatus.CONFLICT)
	public static class TieneFacturasException extends RuntimeException {
		public TieneFacturasException(String clave) {
			super("No se puede eliminar el usuario con clave '" + clave + "' porque tiene facturas");
		}
	}

}
