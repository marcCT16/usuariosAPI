package com.example.practica_api_rest.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class FacturaDTO {

	private Long id;
	private String usuarioClave;
	private LocalDateTime fecha;
	private BigDecimal total;
	private List<DetalleFacturaDTO> productos;

}