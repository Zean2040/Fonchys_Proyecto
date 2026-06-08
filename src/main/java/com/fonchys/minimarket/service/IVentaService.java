package com.fonchys.minimarket.service;

import com.fonchys.minimarket.dto.VentaRequestDTO;
import com.fonchys.minimarket.model.Venta;

import java.util.List;

public interface IVentaService {

    Venta registrarVenta(VentaRequestDTO dto, String emailUsuario);

    List<Venta> listarVentasHoy();

    List<Venta> listarTodas();

    List<Venta> listarPorRangoFecha(String fechaInicio, String fechaFin);

    Venta buscarPorId(Long id);

    void anularVenta(Long id);

    Double totalVentasHoy();
}
