package com.fonchys.minimarket.service.impl;

import com.fonchys.minimarket.model.Proveedor;
import com.fonchys.minimarket.repository.ProveedorRepository;
import com.fonchys.minimarket.service.IProveedorService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProveedorServiceImpl implements IProveedorService {

    private static final Logger logger = LoggerFactory.getLogger(ProveedorServiceImpl.class);

    private final ProveedorRepository proveedorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> listarActivos() {
        return proveedorRepository.findByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> buscarPorNombre(String nombre) {
        return proveedorRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Proveedor> buscarPorId(Long id) {
        return proveedorRepository.findById(id);
    }

    @Override
    @Transactional
    public Proveedor guardar(Proveedor proveedor) {
        Proveedor guardado = proveedorRepository.save(proveedor);
        logger.info("Proveedor creado: id={}, nombre={}", guardado.getId(), guardado.getNombre());
        return guardado;
    }

    @Override
    @Transactional
    public Proveedor actualizar(Long id, Proveedor datos) {
        Proveedor proveedor = proveedorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Proveedor no encontrado: " + id));
        proveedor.setNombre(datos.getNombre());
        proveedor.setRuc(datos.getRuc());
        proveedor.setTelefono(datos.getTelefono());
        proveedor.setEmail(datos.getEmail());
        proveedor.setDireccion(datos.getDireccion());
        Proveedor actualizado = proveedorRepository.save(proveedor);
        logger.info("Proveedor actualizado: id={}", actualizado.getId());
        return actualizado;
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Proveedor no encontrado: " + id));
        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);
        logger.info("Proveedor desactivado: id={}", id);
    }
}
