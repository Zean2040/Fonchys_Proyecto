package com.fonchys.minimarket.service;

import com.fonchys.minimarket.model.Proveedor;

import java.util.List;
import java.util.Optional;

public interface IProveedorService {
    List<Proveedor> listarActivos();
    List<Proveedor> buscarPorNombre(String nombre);
    Optional<Proveedor> buscarPorId(Long id);
    Proveedor guardar(Proveedor proveedor);
    Proveedor actualizar(Long id, Proveedor datos);
    void desactivar(Long id);
}
