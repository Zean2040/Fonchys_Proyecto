package com.fonchys.minimarket.dto;

import com.fonchys.minimarket.model.Venta;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class VentaResponseDTO {

    private Long id;
    private LocalDateTime fecha;
    private BigDecimal total;
    private Venta.Estado estado;
    private String clienteNombre;
    private String clienteDni;
    private String usuarioEmail;
    private List<DetalleResponseDTO> detalles;

    @Data
    public static class DetalleResponseDTO {
        private Long productoId;
        private String productoNombre;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }

    public static VentaResponseDTO desde(Venta venta) {
        VentaResponseDTO dto = new VentaResponseDTO();
        dto.setId(venta.getId());
        dto.setFecha(venta.getFecha());
        dto.setTotal(venta.getTotal());
        dto.setEstado(venta.getEstado());
        dto.setClienteNombre(venta.getClienteNombre());
        dto.setClienteDni(venta.getClienteDni());
        dto.setUsuarioEmail(venta.getUsuario() != null ? venta.getUsuario().getEmail() : null);
        dto.setDetalles(venta.getDetalles().stream().map(d -> {
            DetalleResponseDTO det = new DetalleResponseDTO();
            det.setProductoId(d.getProducto().getId());
            det.setProductoNombre(d.getProducto().getNombre());
            det.setCantidad(d.getCantidad());
            det.setPrecioUnitario(d.getPrecioUnitario());
            det.setSubtotal(d.getSubtotal());
            return det;
        }).collect(Collectors.toList()));
        return dto;
    }
}
