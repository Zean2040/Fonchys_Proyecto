package com.fonchys.minimarket.service.impl;

import com.fonchys.minimarket.dto.VentaRequestDTO;
import com.fonchys.minimarket.model.DetalleVenta;
import com.fonchys.minimarket.model.Producto;
import com.fonchys.minimarket.model.Usuario;
import com.fonchys.minimarket.model.Venta;
import com.fonchys.minimarket.repository.ProductoRepository;
import com.fonchys.minimarket.repository.VentaRepository;
import com.fonchys.minimarket.service.IUsuarioService;
import com.fonchys.minimarket.service.IVentaService;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements IVentaService {

    private static final Logger logger = LoggerFactory.getLogger(VentaServiceImpl.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final IUsuarioService usuarioService;

    @Override
    @Transactional
    public Venta registrarVenta(VentaRequestDTO dto, String emailUsuario) {
        // Apache Commons: validar email
        Preconditions.checkArgument(StringUtils.isNotBlank(emailUsuario), "El email del usuario es requerido");
        Preconditions.checkNotNull(dto, "Los datos de la venta no pueden ser nulos");
        Preconditions.checkArgument(dto.getItems() != null && !dto.getItems().isEmpty(), "La venta debe tener al menos un producto");

        Usuario usuario = usuarioService.buscarPorEmail(emailUsuario)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + emailUsuario));

        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setFecha(LocalDateTime.now());

        for (VentaRequestDTO.ItemDTO item : dto.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductoId()));

            if (producto.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre()
                    + " (disponible: " + producto.getStock() + ")");
            }

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.calcularSubtotal();
            venta.getDetalles().add(detalle);

            // Reducir stock
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);
        }

        venta.calcularTotal();
        Venta guardada = ventaRepository.save(venta);
        logger.info("Venta registrada: id={}, total=S/.{}, items={}, usuario={}",
            guardada.getId(), guardada.getTotal(), guardada.getDetalles().size(), emailUsuario);
        return guardada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> listarVentasHoy() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = LocalDate.now().atTime(23, 59, 59);
        return ventaRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> listarTodas() {
        return ventaRepository.findAllByOrderByFechaDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> listarPorRangoFecha(String fechaInicio, String fechaFin) {
        Preconditions.checkArgument(StringUtils.isNotBlank(fechaInicio), "La fecha de inicio es requerida");
        Preconditions.checkArgument(StringUtils.isNotBlank(fechaFin), "La fecha de fin es requerida");
        LocalDateTime inicio = LocalDate.parse(fechaInicio, DATE_FMT).atStartOfDay();
        LocalDateTime fin = LocalDate.parse(fechaFin, DATE_FMT).atTime(23, 59, 59);
        return ventaRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin);
    }

    @Override
    @Transactional(readOnly = true)
    public Venta buscarPorId(Long id) {
        Preconditions.checkNotNull(id, "El ID no puede ser nulo");
        return ventaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public void anularVenta(Long id) {
        Venta venta = buscarPorId(id);
        if (venta.getEstado() == Venta.Estado.ANULADA) {
            throw new RuntimeException("La venta ya se encuentra anulada");
        }
        // Restaurar stock al anular
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepository.save(producto);
        }
        venta.setEstado(Venta.Estado.ANULADA);
        ventaRepository.save(venta);
        logger.info("Venta anulada: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Double totalVentasHoy() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = LocalDate.now().atTime(23, 59, 59);
        Double total = ventaRepository.sumTotalEntreFechas(inicio, fin);
        return total != null ? total : 0.0;
    }
}
